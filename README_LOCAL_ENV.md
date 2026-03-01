# Backend Local Env Guide

## 1) Prepare `.env`

```bash
cd aini-inu-backend
cp .env.example .env
```

Fill required keys:
- `GEMINI_API_KEY`
- `LOSTPET_CHAT_BASE_URL`, `LOSTPET_CHAT_DIRECT_CREATE_PATH`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

## 2) PostgreSQL + pgvector

Ensure local PostgreSQL is running and execute:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

## 3) Run backend

```bash
./gradlew bootRun
```

`spring-dotenv` loads `.env` automatically during local run.

## 4) Default seed data

At startup, SQL init loads:
- `db/seed/00_lookup_seed.sql` (breed/personality/walking-style/member personality type)
- `db/seed/10_core_sample_seed.sql` (member/pet/walk/chat/lostpet/community sample graph)
- `db/seed/99_reset_sequences.sql` (sequence alignment after fixed-id seed)

If any DDL/seed script fails, startup also fails (`spring.sql.init.continue-on-error=false`).

Useful sample accounts:
- `owner01@test.com` (`id=1`, `PET_OWNER`)
- `owner02@test.com` (`id=2`, `PET_OWNER`)
- `finder05@test.com` (`id=5`, `PET_OWNER`)
- `comm07@test.com` (`id=7`, `NON_PET_OWNER`)

For local development token issuance, you can also use:
- `POST /api/v1/test/auth/token?memberId={id}`

## 5) Swagger / OpenAPI

- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui/index.html`
