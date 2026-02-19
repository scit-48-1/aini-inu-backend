---
name: erd-ddl-sync-checker
description: API/ERD/DDL 문서 간 엔티티, 컬럼, 관계, 제약조건, 인덱스 정합성을 점검하고 누락/충돌을 분류한다. Use when you need to: (1) backend_spec_04_erd.md와 backend_spec_05_ddl.md의 일치성 검증, (2) API 신규 스펙 반영에 필요한 DB 변경 식별, (3) 스펙 문서 간 불일치 리포트 생성.
---

# ERD DDL Sync Checker

ERD와 DDL, 필요 시 API까지 교차 검증해 문서 정합성을 확보한다.

## Workflow

1. 비교 기준을 고정한다.
   - 기준 우선순위: API 계약 → ERD → DDL
2. 엔티티/테이블 매핑을 만든다.
   - 엔티티명, PK/FK, 관계 cardinality를 표로 정리한다.
3. 컬럼/타입/nullable/default를 비교한다.
4. 인덱스/유니크/삭제 정책을 비교한다.
5. 충돌 항목을 분류한다.
   - `Spec Missing`, `Schema Missing`, `Type Mismatch`, `Constraint Mismatch`로 태깅한다.

## Output Contract

아래 5개 섹션을 항상 같은 순서로 출력한다.

1. `입력 범위`
2. `결정 사항`
3. `불일치 목록`
4. `사용자 확인 필요 항목`
5. `다음 에이전트 전달 요약(10줄 이내)`

## Artifact Path

- 기본 산출물: `spec-docs/working/erd_ddl_sync_report.md`
- 체크리스트: `references/sync-checklist.md`

## Rules

- 매핑이 불가능한 필드는 임의 치환하지 않는다.
- DDL에만 존재하는 컬럼은 이유(운영/감사/최적화)를 확인 질문으로 남긴다.
- 스키마 변경 제안 시 영향 범위(API/쿼리/마이그레이션)를 함께 표기한다.

