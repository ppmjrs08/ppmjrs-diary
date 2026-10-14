import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import vm from 'node:vm';

class Node {
  constructor(){this.children=[];this.listeners={};this.style={};this.value='';this.hidden=false;this.disabled=false;this.dataset={};this.ownText='';}
  set textContent(s){this.ownText=String(s);this.children=[];}
  get textContent(){return this.ownText+this.children.map(x=>x.textContent).join(' ');}
  append(...nodes){this.children.push(...nodes);}
  replaceChildren(...nodes){this.ownText='';this.children=nodes;}
  addEventListener(name,f){this.listeners[name]=f;}
  focus(){}
}
function deferred(){let resolve;const promise=new Promise(r=>resolve=r);return {promise,resolve};}
async function setup(){
  const nodes=new Map(),el=id=>{if(!nodes.has(id))nodes.set(id,new Node());return nodes.get(id);};
  let user=null,delayPath=null,oldResponse=null;const started=deferred();
  const response=(data,status=200)=>({ok:status===200,status,json:async()=>status===200?{data}:{message:'Please sign in'}});
  const context=vm.createContext({document:{getElementById:el,createElement:()=>new Node(),querySelectorAll:()=>[]},AbortController,setTimeout,clearTimeout,console,
    fetch:async(path,options)=>{
      if(path===delayPath){oldResponse=deferred();delayPath=null;started.resolve();return oldResponse.promise;}
      if(path==='/api/auth/session')return response(null,401);
      if(path==='/api/auth/login'){user=JSON.parse(options.body).username;return response({studentId:user,csrfToken:user});}
      if(path==='/api/auth/logout'){user=null;return response({});}
      if(path==='/api/m2/me')return response({completed:[],profile:{interests:'java',goals:''}});
      if(path==='/api/m2/courses')return response([]);
      if(path==='/api/m2/history')return response([]);
      if(path==='/api/m2/recommendations')return response({items:[]});
      throw new Error('Unexpected request '+path);
    }});
  vm.runInContext(readFileSync(new URL('src/main/resources/static/app.js',import.meta.url),'utf8'),context);
  await new Promise(r=>setImmediate(r));
  el('password').value='demo-password';el('topK').value='3';
  const submit=id=>el(id).listeners.submit({preventDefault(){},submitter:new Node()});
  const login=async name=>{el('username').value=name;await submit('login-form');};
  return {el,submit,login,delay(path){delayPath=path;},started:started.promise,resolve(data){oldResponse.resolve(response(data));},logout:()=>el('logout').listeners.click()};
}
test('late history cannot be shown after switching accounts',async()=>{
  const h=await setup();h.delay('/api/m2/history');const old=h.login('student1');await h.started;
  await h.logout();await h.login('student2');
  h.resolve([{query:'PRIVATE STUDENT ONE HISTORY',createdAt:'2026-10-14T12:00:00',result:{items:[]}}]);await old;
  assert.equal(h.el('account-label').textContent,'student2 · 演示学生');
  assert.ok(!h.el('history-list').textContent.includes('PRIVATE STUDENT ONE HISTORY'));
});
test('late recommendation cannot be shown after switching accounts',async()=>{
  const h=await setup();await h.login('student1');h.delay('/api/m2/recommendations');
  const old=h.submit('recommend-form');await h.started;await h.logout();await h.login('student2');
  h.resolve({items:[{course:{id:'X',title:'PRIVATE STUDENT ONE RESULT',description:'private',prerequisites:[]},matchingTerms:['java'],score:1}]});await old;
  assert.ok(!h.el('results-list').textContent.includes('PRIVATE STUDENT ONE RESULT'));
  assert.equal(h.el('recommend-button').disabled,false);
});
