# M2 Implementation Plan

**Goal:** 可运行的 Java 课程推荐模块和公开 GitHub 源码。
**Architecture:** Java 算法与 Web/存储解耦，身份来自服务端；演示 H2，团队 MySQL。
**Spec:** design.md

## Global Constraints
最多三门；已修与先修从服务端数据读取；仅本人历史；虚构数据；不提交凭据、缓存、参考工程。

## Review Focus
并列排序稳定；未知词空结果；全部先修满足才合格；伪造已修字段无效；生产模式演示登录关闭。

## Task 1: 推荐算法
- [x] 写 RecommenderTest，观察缺失实现导致失败。
- [x] Course 与 Recommender.rank(List<Course>, String, Set<String>, int)；TF-IDF 与关键词基线。
- [x] 全量算法测试通过。

## Task 2: 存储、身份、接口
- [x] 写真实 HTTP 流程测试，观察缺失应用失败。
- [x] MyBatis-Plus Mapper，demo H2种子，mysql外部配置；StudentIdentity 解析 session 或配置 token。
- [x] /api/m2/me、/courses、/recommendations、/history；统一错误响应，严格 JSON，CSRF，私有数据不缓存。
- [x] 登录/权限/过滤/保存/坏输入 HTTP 测试通过。

## Task 3: 页面、交接与发布
- [x] 页面真实调用接口，浏览器验收并保存截图。
- [x] SQL、API/用例/数据说明、评价集、测试报告、启动脚本、README。
- [x] 构建 Jar、冷启动、重启持久化、独立审阅修复。
- [ ] 公开仓库上传源码，远端核验提交和文件。
