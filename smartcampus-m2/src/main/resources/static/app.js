'use strict';
const $=id=>document.getElementById(id);
let csrf='', courses=[], completed=new Set(), generation=0;
const inflight=new Set();
function nextGeneration(){generation++;for(const controller of inflight)controller.abort();inflight.clear();return generation;}
function stale(){const e=new Error('Account changed');e.name='StaleResponse';return e;}
function element(tag,text,cls){const node=document.createElement(tag);if(text!==undefined)node.textContent=text;if(cls)node.className=cls;return node;}
function error(message){$('status').textContent=message;$('status').hidden=!message;}
async function api(path,options={}){
  const owner=generation,controller=new AbortController(), timer=setTimeout(()=>controller.abort(),12000);inflight.add(controller);
  try{
    const response=await fetch(path,{...options,signal:controller.signal,headers:{'Content-Type':'application/json',...(csrf?{'X-CSRF-Token':csrf}:{}),...options.headers}});
    const payload=await response.json();
    if(owner!==generation)throw stale();
    if(!response.ok){if(response.status===401&&path!=='/api/auth/login') signedOut();throw new Error(payload.message||'请求失败，请重试。');}
    return payload.data;
  }catch(e){if(owner!==generation)throw stale();if(e.name==='AbortError')throw new Error('请求超时，请检查服务后重试。');throw e;}
  finally{clearTimeout(timer);inflight.delete(controller);}
}
function signedOut(){nextGeneration();csrf='';$('workspace').hidden=true;$('login-panel').hidden=false;$('logout').hidden=true;$('account-label').textContent='未登录';$('history-list').replaceChildren();$('results-list').replaceChildren();$('recommend-button').disabled=false;$('recommend-button').textContent='生成我的推荐 ↗';}
async function signedIn(data){
  const owner=nextGeneration();
  csrf=data.csrfToken;$('account-label').textContent=data.studentId+' · 演示学生';
  const [me,list]=await Promise.all([api('/api/m2/me'),api('/api/m2/courses')]);
  if(owner!==generation)throw stale();
  courses=list;completed=new Set(me.completed);$('interests').value=me.profile.interests;$('goals').value=me.profile.goals;
  $('catalog-count').textContent=courses.length;$('completed-count').textContent=completed.size;
  $('completed-badges').replaceChildren(...[...completed].map(id=>element('span',id,'badge')));
  if(!completed.size)$('completed-badges').append(element('span','暂无已修课程','badge'));
  renderCatalog();$('login-panel').hidden=true;$('workspace').hidden=false;$('logout').hidden=false;
  renderResults(null);await history();
}
function renderResults(result){
  const root=$('results-list');root.replaceChildren();
  $('result-count').textContent=result?result.items.length+' 门课程':'等待生成';
  if(!result||!result.items.length){const box=element('div',undefined,'empty');box.append(element('span','↗'),element('h3',result?'暂时没有合适的课程':'从一个兴趣开始'),element('p',result?'尝试其他英文关键词；匹配课程可能已修完或尚未满足先修条件。':'输入兴趣，查看课程与推荐理由。'));root.append(box);return;}
  result.items.forEach((item,i)=>{
    const card=element('article',undefined,'course-card'),top=element('div',undefined,'course-top');
    top.append(element('span',item.course.id,'course-id'),element('span','0'+(i+1),'rank'));
    card.append(top,element('h3',item.course.title),element('p',item.course.description));
    const score=element('div',undefined,'score-row'),bar=element('div',undefined,'score-bar'),fill=element('div',undefined,'score-fill');
    fill.style.width=(item.score*100).toFixed(1)+'%';bar.append(fill);score.append(element('span','文本相似度'),bar,element('span',item.score.toFixed(3)));card.append(score);
    const reason=element('div',undefined,'reason');reason.append(element('strong','为什么推荐这门课'),element('div','匹配关键词：'+item.matchingTerms.join(' · ')),element('div',item.course.prerequisites.length?'已满足先修课程：'+item.course.prerequisites.join(', '):'无先修要求，可直接开始学习'));card.append(reason);root.append(card);
  });
}
function renderCatalog(){
  $('catalog-body').replaceChildren(...courses.map(c=>{
    const row=element('tr'),done=completed.has(c.id),missing=c.prerequisites.filter(x=>!completed.has(x));
    row.append(element('td',c.id),element('td',c.title),element('td',c.prerequisites.length?c.prerequisites.join(', '):'无'));
    row.append(element('td',done?'已修完':missing.length?'缺少 '+missing.join(', '):'满足先修条件',done?'muted':missing.length?'blocked':'eligible'));return row;
  }));
}
async function history(){
  const owner=generation;
  const rows=await api('/api/m2/history');
  if(owner!==generation)throw stale();
  $('history-list').replaceChildren(...rows.map(h=>{
    const row=element('div',undefined,'history-row'),text=element('div');text.append(element('strong',h.query),element('p',h.result.items.length?h.result.items.map(x=>x.course.id+' '+x.course.title).join(' / '):'没有匹配的合格课程'));
    row.append(text,element('time',h.createdAt.replace('T',' ').slice(0,19)));return row;
  }));
  if(!rows.length)$('history-list').append(element('p','还没有推荐记录。生成一次推荐后，会保存在这里。','muted'));
}
$('login-form').addEventListener('submit',async e=>{
  e.preventDefault();error('');const button=e.submitter;button.disabled=true;
  try{const data=await api('/api/auth/login',{method:'POST',body:JSON.stringify({username:$('username').value,password:$('password').value})});await signedIn(data);}catch(e){if(e.name!=='StaleResponse')error(e.message);}finally{button.disabled=false;}
});
$('recommend-form').addEventListener('submit',async e=>{
  e.preventDefault();error('');const owner=generation,button=$('recommend-button');button.disabled=true;button.textContent='正在寻找合适的课程…';renderResults(null);
  try{const result=await api('/api/m2/recommendations',{method:'POST',body:JSON.stringify({interests:$('interests').value,goals:$('goals').value,topK:Number($('topK').value)})});if(owner!==generation)throw stale();renderResults(result);await history();}catch(e){if(e.name!=='StaleResponse')error(e.message);}finally{if(owner===generation){button.disabled=false;button.textContent='生成我的推荐 ↗';}}
});
$('logout').addEventListener('click',async()=>{try{await api('/api/auth/logout',{method:'POST',body:'{}'});signedOut();error('');}catch(e){if(e.name!=='StaleResponse')error(e.message);}});
$('refresh-history').addEventListener('click',()=>history().catch(e=>{if(e.name!=='StaleResponse')error(e.message);}));
document.querySelectorAll('[data-interest]').forEach(button=>button.addEventListener('click',()=>{$('interests').value=button.dataset.interest;$('interests').focus();}));
(async()=>{try{await signedIn(await api('/api/auth/session'));}catch(e){if(e.name!=='StaleResponse'&&e.message!=='Please sign in')error(e.message);}})();
