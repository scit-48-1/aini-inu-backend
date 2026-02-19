---
name: ui-flow-extractor
description: Frontend UI/UX 구조에서 화면, 사용자 액션, 상태 흐름, API 호출을 추출하고 모듈/컴포넌트 단위로 분해한다. Use when you need to: (1) 기존 프론트 코드의 정보구조/네비게이션을 문서화, (2) 화면별 컴포넌트 트리와 책임 분리, (3) UX 동작 기준의 API 호출 지점 정리, (4) 백엔드 계약 매핑을 위한 입력 자료 생성.
---

# UI Flow Extractor

프론트엔드 코드를 재구축 가능한 수준으로 분해해 계약 매핑의 입력 자료를 만든다.

## Workflow

1. 분석 범위를 고정한다.
   - 우선순위 도메인(예: Threads, Chat, LostPets, Diary)을 명시한다.
   - 라우트 기준으로 분석 단위를 나눈다.
2. 라우트/화면 구조를 추출한다.
   - `src/app/**/page.tsx`, `layout.tsx`에서 화면 엔트리를 수집한다.
3. 컴포넌트 책임을 분해한다.
   - `src/components/**`를 화면 전용/공용/상태 보유 컴포넌트로 분류한다.
4. 상태/동작/API 호출을 연결한다.
   - `src/hooks/**`, `src/stores/**`, `src/services/api/**`를 따라 사용자 액션 → 상태 변경 → API 호출 순서를 정리한다.
5. 불일치 가능성을 태깅한다.
   - 임시/mock 호출, 스펙에 없는 경로, 필드명 차이를 별도 표로 만든다.

## Output Contract

아래 5개 섹션을 항상 같은 순서로 출력한다.

1. `입력 범위`
2. `결정 사항`
3. `불일치 목록`
4. `사용자 확인 필요 항목`
5. `다음 에이전트 전달 요약(10줄 이내)`

## Artifact Path

- 기본 산출물: `spec-docs/working/ui_flow_extraction.md`
- 체크리스트: `references/extraction-checklist.md`

## Rules

- 추측으로 확정하지 않는다. 근거 파일 경로를 함께 남긴다.
- 동일 기능의 중복 컴포넌트는 통합 후보로 태깅만 하고 즉시 병합하지 않는다.
- 미확정 항목은 반드시 질문 형태로 남긴다.
