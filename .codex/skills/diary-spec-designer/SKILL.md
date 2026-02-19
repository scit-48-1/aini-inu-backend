---
name: diary-spec-designer
description: 신규 Diary 도메인의 API/ERD/DDL 스펙을 설계하고 기존 Threads/Chat/LostPets와의 관계를 정의한다. Use when you need to: (1) 스펙에 없는 Diary 기능 신규 추가, (2) 화면 UX 요구를 계약 가능한 API로 정리, (3) ERD/DDL 테이블 및 관계 설계, (4) 미확정 정책 항목 질문 정리.
---

# Diary Spec Designer

기존 스펙 체계를 유지하면서 Diary 도메인을 신규 정의한다.

## Workflow

1. 요구사항 경계를 정의한다.
   - 작성/조회/수정/삭제/공개범위/권한/첨부파일 정책을 먼저 확정한다.
2. API 스펙 초안을 작성한다.
   - 목록/상세/생성/수정/삭제 + 검색/필터/정렬이 필요한지 판단한다.
3. ERD를 설계한다.
   - `WALK_DIARY` 중심으로 사진/태그/연결 엔티티를 설계한다.
4. DDL을 설계한다.
   - PK/FK, 인덱스, soft delete, 감사 컬럼, 제약조건을 명시한다.
5. 기존 도메인과 연결한다.
   - Thread, Member, Chat과 참조 관계를 명확히 기술한다.

## Output Contract

아래 5개 섹션을 항상 같은 순서로 출력한다.

1. `입력 범위`
2. `결정 사항`
3. `불일치 목록`
4. `사용자 확인 필요 항목`
5. `다음 에이전트 전달 요약(10줄 이내)`

## Artifact Path

- API 반영: `spec-docs/backend_spec_03_api.md`
- ERD 반영: `spec-docs/backend_spec_04_erd.md`
- DDL 반영: `spec-docs/backend_spec_05_ddl.md`
- 정책 질문 목록: `references/decision-points.md`

## Rules

- 기존 명명 규칙과 `ApiResponse<T>` 응답 래핑 규칙을 유지한다.
- 미확정 정책은 스펙에 TODO로 남기지 말고 질문 목록으로 분리한다.
- 구현 영향(백엔드/프론트/마이그레이션)을 한 줄씩 명시한다.

