# M2 验证与评价记录

日期：2026-10-14；源码 Java17目标，本机Java25 / Maven3.9.16 / Windows11。

## 自动化验证
33项Java测试全部通过，2项前端账号切换竞态测试全部通过。

| 测试类 | 项数 | 覆盖 |
|---|---:|---|
| RecommenderTest | 11 | TF-IDF、资格、全先修、已修/停用、空语料/未知词/停用词、大小写、输入/数量、并列稳定排序、同资格基线 |
| ApiTest | 15 | 真实HTTP服务器/H2：登录、坏凭据、个人资料、推荐、未知词、输入、未知字段、history隔离、CSRF、退出、页面；分号路径及404回归 |
| IdentityTest | 3 | 正确Bearer、错误token/学生头不能冒充、短token拒绝 |
| ProfileModeTest | 2 | demo+mysql混用启动失败；mysql只有token身份、无demo登录bean |
| PersistenceTest | 1 | 4个同步首次请求：1条兴趣档案、4条历史，全部成功 |
| EvaluationTest | 1 | 20合成档案×2方法，无已修/先修违规、数量≤3 |
| frontend-test.mjs | 2 | 延迟历史与推荐在退出/换账号后不显示；新账号按钮仍可用 |

独立审阅发现4个重要问题，均先复现失败再修复：原始URL与MVC路径不一致导致认证/CSRF绕过；混用demo/mysql启用演示认证；初次档案插入竞态；延迟响应串账号。路径已改用Servlet规范化路径，demo bean排除mysql，兴趣使用原子upsert，前端使用账号generation和取消请求。SQL与历史保存同一事务。错误后的旧推荐卡片也已清除。

测试真实使用HTTP和数据库，不使用Mockito；移除未使用的模拟依赖避免中文路径辅助进程编码问题。混合profile测试刻意触发启动失败，因此该测试日志中的拒绝启动警告为预期。

完整Java测试后的JAR封装曾被正在运行的演示JAR占用；停止该演示后重新封装成功。此后没有跳过失败测试；封装阶段使用已通过的同一源码。Windows修改源码后先停止服务，再构建。

## 合成档案评价
数据：20门英文虚构课程；`src/test/resources/evaluation-profiles.tsv` 有20个兴趣/已修/相关课程标签。标签为项目作者合成，**没有独立学生或专家复核**。测试标签不参与TF-IDF语料拟合。结果见 [evaluation-results.csv](evaluation-results.csv)。

TF-IDF和关键词基线均采用相同资格过滤。两者在本集合返回相同顺序，共返回26条，22条被合成标签标为相关，micro precision among returned = 22/26 = 0.8462；各有1个档案为空结果。无已修推荐，先修违规0。少于三条时以实际返回数为分母，空列表为NA；不把空列表计作100%精确率。

结果不证明TF-IDF优于基线。P10等档案可返回词汇有重叠但标签不相关的课程；课程文本重叠、精确词匹配、英文词形变化和未知同义词是局限。20档案不能说明真实学生满意度，也不能替代proposal中计划的独立审阅。

## 运行验收
浏览器实际登录student1，输入machine learning：返回CS201，匹配词learning/machine，先修CS101满足，相似度约0.788；CS301因缺CS201不出现。页面真实加载20门课程及个人历史，无浏览器控制台错误。截图为实际运行。

已实测重启运行包并重新登录：2026-10-14 15:55:25 的旧推荐记录仍保留，新生成记录也成功保存。真实MySQL环境、团队统一账号、Vue集成、另一台电脑复现和真实用户验收尚未验证。

复现：`mvn test` + `node --test frontend-test.mjs`；`mvn package`；`run.ps1`启动；改变账号、兴趣和已修演示记录后观察资格。需要真实MySQL时由数据库成员先核对契约并提供临时库。
