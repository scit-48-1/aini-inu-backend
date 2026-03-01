# Backend Local Env Guide

## 1) Prepare `.env`

```bash
cd aini-inu-backend
cp .env.example .env
```

Fill required keys:
- `GEMINI_API_KEY`
- `LOSTPET_AI_BASE_URL`, `LOSTPET_AI_ANALYZE_PATH`
- `LOSTPET_CHAT_BASE_URL`, `LOSTPET_CHAT_DIRECT_CREATE_PATH`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

## 2) PostgreSQL + pgvector

Ensure local PostgreSQL is running and execute:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## 3) Run backend

```bash
./gradlew bootRun
```

`spring-dotenv` loads `.env` automatically during local run.
