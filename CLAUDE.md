# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

拾壹博客 (Shiyi Blog) — a full-stack blog platform. Three frontends + one Spring Boot backend.

## Architecture

### Backend (`blog/` — Java 17, Spring Boot 2.7.0, Maven multi-module)

Main class: `com.mojian.NeatAdminApplication` (port 8800)

| Module | Purpose |
|---|---|
| `mojian-server` | Boot entry point, resources (application.yml, MyBatis mappers, templates) |
| `mojian-commom` | Shared: entities (`SysArticle`, `SysComment`, `SysUser`, etc.), DTOs, VOs, mappers, enums, utils, WebSocket |
| `mojian-admin` | Admin panel controllers + services (article, dashboard, message, monitor, site, system, tool) |
| `mojian-api` | Public-facing portal controllers + services (album, app, article, chat, comment, friend, message, moment, resource, user) |
| `mojian-auth` | Authentication (Sa-Token + social login: QQ/Weibo/Gitee/GitHub/WeChat) |
| `mojian-ai` | AI module: LangChain4j with OpenAI-compatible chat models (DeepSeek, ModelScope/Qwen, Ollama), streaming chat, RAG knowledge loaders |
| `mojian-file` | File storage abstraction (FastDFS, local-plus, Qiniu OSS via x-file-storage) |
| `mojian-quartz` | Scheduled job management |
| `mojian-wx` | WeChat Official Account integration |

Key dependencies: MyBatis-Plus 3.5.2, Sa-Token 1.39.0, MySQL, Redis, Knife4j/Swagger.
Note: article search is plain MySQL via MyBatis-Plus (`keyword` param) — there is no Elasticsearch dependency in the current code, despite the README badges.

### Frontend — Admin Panel (`blog-admin/` — Vue 3 + TypeScript + Vite + Element Plus + Pinia)

- Port 3000, proxies `/api` -> `http://127.0.0.1:8800/`
- Auto-imports for Vue/Vue Router/Pinia via unplugin-auto-import
- API layer at `src/api/` organized by domain (ai, article, file, message, monitor, site, system, tool)
- State management: Pinia with persisted state
- Views mirror backend domains: article, message, monitor, site, system, tool, dashboard

### Frontend — Portal (`blog-web/` — Vue 2 + Vite + Element UI + Vuex)

- Port 3000 (separate dev server from admin)
- API layer at `src/api/` (article, auth, chat, comment, friends, message, moments, etc.)
- State: Vuex stores
- Views: home, article, archives, categories, tags, photos, messages, moments, chat, resources, friends, about, profile, notifications
- WebSocket URL and API endpoints configured in `.env.development`

> **Both `blog-admin` and `blog-web` are configured for port 3000** — they cannot run at the same time; start whichever one you are working on.

### Frontend — Astro Blog Template (`gblog-gblog-template/` — Astro + Drizzle ORM)

- Statically-generated blog backed by **MySQL** (Drizzle `dialect: 'mysql'`, `mysql2` driver) — not SQLite
- Schema and migrations live in `drizzle/` (`schema.ts` + generated SQL migrations)
- Separate standalone project, not connected to the Spring Boot backend
- Lint via `pnpm lint` / `pnpm lint:fix` (ESLint)

## Database

- SQL dump files at root: `mj-blog.sql`, `shiyi_blog.sql`
- Key entities: `SysArticle`, `SysCategory`, `SysTag`, `SysComment`, `SysUser`, `SysMenu`, `SysRole`, `SysConfig`, `SysPhoto`, `SysAlbum`, `SysMoment`, `SysMessage`, `SysFeedback`, `SysFriend`, `SysNotice`, `SysDict`, `SysJob`, `SysOperateLog`

## Common Commands

### Backend (from `blog/`)
```bash
# Build (tests skipped)
mvn clean package -DskipTests

# Run
mvn spring-boot:run -pl mojian-server

# Package single module
mvn package -pl mojian-server -am -DskipTests
```

### Frontend Admin (from `blog-admin/`)
```bash
npm run dev      # Start dev server on port 3000
npm run build    # Build for production
```

### Frontend Portal (from `blog-web/`)
```bash
npm run dev      # Start dev server on port 3000
npm run build    # Build for production
```

### Astro Template (from `gblog-gblog-template/`)
```bash
pnpm run dev     # Start dev server
pnpm run build   # Build for production
pnpm lint        # ESLint
```

## Key Configuration Files

- Backend: `blog/mojian-server/src/main/resources/application.yml` (main config; active profile: dev)
- Backend: `blog/mojian-server/src/main/resources/application-dev.yml` (dev config: DB, Redis, OAuth, AI, file storage, mail)
- Admin: `blog-admin/.env.development` (API proxy target, app title)
- Portal: `blog-web/.env.development` (API endpoints, WebSocket URL)

## Important Notes

- Run the backend first, then the frontend — frontend config is dynamically loaded from the backend.
- Admin login: admin / 123456 (local dev).
- API docs available at `http://127.0.0.1:8800/shiyi/doc.html` (swagger/knife4j).
- MyBatis XML mappers live in `mojian-server/src/main/resources/mapper/`.
- File upload is via `x-file-storage`; `default-platform: fastdfs` with a `local-plus` platform also configured. The `local-plus/` directory at the repo root is the local file storage path (x-file-storage `local-plus-1` platform), not a source module.
- Logs are written to `./logs/mojian.log` with daily rolling.
- 不要做git操作
