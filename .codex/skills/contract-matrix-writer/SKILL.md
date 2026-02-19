---
name: contract-matrix-writer
description: 프론트 화면 동작과 백엔드 API 명세를 1:1로 연결하는 계약표를 작성/유지한다. Use when you need to: (1) frontend_api_contract_matrix.md 생성, (2) 프론트 호출과 스펙 엔드포인트 매핑, (3) 불일치 항목의 의사결정 상태 관리, (4) 도메인별 계약 합의 추적.
---

# Contract Matrix Writer

프론트-백엔드 간 계약 불일치를 가시화하고 결정 상태를 관리한다.

## Workflow

1. 입력 자료를 수집한다.
   - UI Flow 추출 결과 + API 정규화 결과를 사용한다.
2. 계약표를 작성한다.
   - 화면 액션 기준으로 `현재 프론트 호출`과 `스펙 엔드포인트`를 나란히 적는다.
3. 상태를 분류한다.
   - `Aligned`, `Frontend Patch`, `Spec Update`, `Decision Needed`
4. 의사결정 로그를 남긴다.
   - 결정자, 날짜, 근거, 후속 작업을 기록한다.
5. 미결 항목을 질문으로 묶는다.

## Output Contract

아래 5개 섹션을 항상 같은 순서로 출력한다.

1. `입력 범위`
2. `결정 사항`
3. `불일치 목록`
4. `사용자 확인 필요 항목`
5. `다음 에이전트 전달 요약(10줄 이내)`

## Artifact Path

- 계약표: `spec-docs/frontend_api_contract_matrix.md`
- 템플릿: `references/matrix-template.md`

## Rules

- 한 행은 하나의 사용자 액션에만 대응시킨다.
- `Decision Needed` 항목은 반드시 질문 ID를 부여한다.
- 결정되지 않은 항목을 구현 확정으로 표기하지 않는다.

