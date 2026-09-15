## 博客介绍

<p align="center">
   <a target="_blank" href="https://github.com/X1192176811/blog">
      <img src="https://img.shields.io/hexpm/l/plug.svg"/>
      <img src="https://img.shields.io/badge/JDK-17+-green.svg"/>
      <img src="https://img.shields.io/badge/SpringBoot-2.7.0-green"/>
      <img src="https://img.shields.io/badge/MyBatisPlus-3.5.2-green"/>
      <img src="https://img.shields.io/badge/SaToken-1.39.0-green"/>
      <img src="https://img.shields.io/badge/MySQL-8.0-green"/>
      <img src="https://img.shields.io/badge/Redis-6.0.5-green"/>
      <img src="https://img.shields.io/badge/Vue3-3.2-green"/>
      <img src="https://img.shields.io/badge/Vue2-2.7-green"/>
      <img src="https://img.shields.io/badge/ElementPlus-2.3-green"/>
   </a>
</p>
[目录结构](#目录结构) | [项目特点](#项目特点) | [技术介绍](#技术介绍) | [运行环境](#运行环境)

基于Gitee https://gitee.com/quequnlong/shiyi-blog 



**Github地址：** [[ycy1/GBLOG: GBLOG](https://github.com/ycy1/GBLOG)](https://gitee.com/quequnlong/shiyi-blog)

您的star是我坚持的动力，感谢大家的支持，欢迎提交pr共同改进项目。

## 目录结构

前端项目 `blog-web` 为前台门户，`blog-admin` 为后台管理系统。

后端项目位于 `blog` 下，采用 Maven 多模块结构。

SQL文件位于根目录下的 `shiyi_blog.sql`。

可直接导入该项目于本地，修改后端配置文件中的数据库等连接信息，项目中使用到的关于七牛云功能和第三方授权登录等需要自行开通。

当你克隆项目到本地后可使用账号：admin，密码：123456 进行登录

本地访问接口文档地址：http://127.0.0.1:8800/shiyi/doc.html

**ps：请先运行后端项目，再启动前端项目，前端项目配置由后端动态加载。**

```
blog
├── mojian-admin    --  后台管理接口模块（文章、系统、监控、订单等）
├── mojian-api      --  门户接口模块（文章、评论、相册、聊天、标签等）
├── mojian-auth     --  认证模块（Sa-Token + 第三方社交登录）
├── mojian-ai       --  AI模块（LangChain4j + RAG知识库 + 多Agent对话）
├── mojian-commom   --  通用模块（实体、DTO、VO、Mapper、枚举、工具类）
├── mojian-file     --  文件模块（多平台存储：本地/七牛/阿里/腾讯/MinIO）
├── mojian-quartz   --  定时任务模块（Quartz）
├── mojian-wx       --  微信公众号模块（群发、菜单、素材、微信支付）
├── mojian-server   --  博客启动类模块（配置文件、资源文件）
```

## 项目特点

### 内容管理
- 采用 Markdown 编辑器，支持代码高亮和图片预览
- 文章分类、标签管理，支持文章目录和推荐文章
- 评论系统支持表情输入回复
- 相册管理，支持图片标签和上传
- 动态/说说功能，支持点赞互动
- 公告通知、友情链接管理

### 用户与权限
- 前后端分离部署，RESTful API 设计
- 基于 Sa-Token 的 RBAC 动态权限模型，前端菜单和后台权限实时更新
- 接入 QQ、微博、Gitee、GitHub、微信等第三方社交登录
- 部门组织架构管理

### 支付与变现
- 文章付费阅读功能，支持自定义收费规则
- 接入微信支付和支付宝，支持订单管理与退款

### AI 智能助手
- 基于 LangChain4j 的 AI 对话功能，支持多 Agent 配置
- RAG 知识库检索增强，支持数据库和文档两种知识源
- 流式输出（SSE），支持对话记忆和上下文管理
- 兼容 DeepSeek、通义千问、Ollama 等多种模型

### 互动与通信
- 基于 WebSocket 的实时群聊功能，支持表情、图片、文件发送
- 弹幕式留言墙，炫酷展示
- 站内通知系统，支持定向推送
- 邮件通知功能

### 系统运维
- 仪表盘数据统计
- 服务器监控（CPU/内存/磁盘），基于 OSHI
- 在线用户管理、Redis 缓存管理
- 操作日志审计（AOP 注解实现）
- 定时任务管理（Quartz）
- 代码生成器（Velocity 模板，一键生成 CRUD 代码）

### 文件与存储
- 多平台文件上传（本地、FastDFS、七牛云、阿里云OSS、腾讯云COS、MinIO）
- 统一文件存储抽象层，可配置切换

### 其他
- 支持代码高亮、深色模式、动态背景
- 门户集成音乐播放器（APlayer）
- 支持 Excel 导出、Word 文档生成、PDF 解析
- 二维码生成功能
- IP 归属地解析（ip2region）
- 微信公众号管理（素材、菜单、群发、二维码等）




## 技术介绍

### 后端技术栈

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Java | 17 | 编程语言 |
| Spring Boot | 2.7.0 | 核心框架 |
| MyBatis-Plus | 3.5.2 | ORM 持久层框架 |
| Sa-Token | 1.39.0 | 轻量级权限认证框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 6.0.5 | 缓存 / 会话存储 |
| Knife4j | 4.4.0 | 接口文档（OpenAPI） |
| Hutool | 5.8.26 | Java 工具类库 |
| JustAuth | 1.16.7 | 第三方社交登录 |
| LangChain4j | 1.0.0-beta3 | AI / LLM 应用开发框架 |
| Quartz | - | 定时任务调度 |
| OSHI | 6.4.0 | 系统硬件监控 |
| Velocity | 2.3 | 代码生成模板引擎 |
| x-file-storage | 2.2.1 | 多平台文件存储 |
| WebSocket | - | 实时通信 |
| Flexmark | 0.62.2 | Markdown 解析 |
| ZXing | 3.5.3 | 二维码生成 |
| ip2region | 2.7.0 | IP 归属地解析 |

### 前端技术栈

**后台管理系统（blog-admin）：**

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Vue | 3.2.47 | 前端框架 |
| TypeScript | 5.4.5 | 类型安全 |
| Vite | 6.3.5 | 构建工具 |
| Element Plus | 2.3.0 | UI 组件库 |
| Pinia | 2.0.33 | 状态管理 |
| ECharts | 5.5.1 | 图表库 |
| WangEditor | 5.1.23 | 富文本编辑器 |
| Mavon Editor | 3.0.1 | Markdown 编辑器 |

**门户前端（blog-web）：**

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Vue | 2.7.16 | 前端框架 |
| Vuex | 3.6.2 | 状态管理 |
| Vite | 5.1.4 | 构建工具 |
| Element UI | 2.15.14 | UI 组件库 |
| Mavon Editor | 2.10.4 | Markdown 编辑器 |
| APlayer | 1.10.1 | 音乐播放器 |
| GSAP | 3.12.5 | 动画库 |
| vue-danmaku | 1.7.2 | 弹幕组件 |

## 运行环境

**服务器：** 腾讯云2核4G CentOS7.6

**对象存储：** 七牛云OSS

## 开发环境

|            开发工具            |           说明            |
| ----------------------------- | ------------------------- |
| IDEA                          | Java开发工具IDE            |
| VSCode                        | Vue开发工具IDE             |
| Navicat                       | MySQL远程连接工具          |
| Another Redis Desktop Manager | Redis远程连接工具          |
| finalshell                    | Linux远程连接和文件上传工具 |

| 开发环境 | 版本   |
| -------- | ------ |
| JDK      | 17     |
| MySQL    | 8.0    |
| Redis    | 6.0.5  |
| Node.js  | 16+    |
