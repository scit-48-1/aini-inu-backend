# API Spec Normalization Template

| Domain | Method | Path | Auth | Request Fields | Response Fields | Error Codes | Notes |
|---|---|---|---|---|---|---|---|
| Thread | POST | /threads | Required | ... | ... | ... | ... |

## Field Rules
- request/response는 `field(type, required)` 형식
- enum은 `field(enum: A\|B\|C)`로 표기
- nested object는 `parent.child`로 평탄화

## Ambiguity Rules
- 동일 path의 서로 다른 설명이 있으면 둘 다 기록
- 응답 예시와 필드 설명이 다르면 `Notes`에 충돌 표시

