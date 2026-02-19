---
name: api-spec-reader
description: API 명세 문서를 정규화된 계약 단위로 읽어 도메인별 엔드포인트, 요청/응답 필드, 인증, 에러코드, 제약조건을 추출한다. Use when you need to: (1) backend_spec_03_api.md를 프론트 계약표 입력 형태로 변환, (2) 도메인별 API 커버리지 확인, (3) 프론트 호출과 비교 가능한 기준 데이터 생성.
---

# API Spec Reader

`spec-docs/backend_spec_03_api.md`를 계약 매핑 가능한 구조로 변환한다.

## Workflow

1. 대상 도메인을 고정한다.
   - 예: Threads, Chat, LostPets, Diary
2. 엔드포인트를 정규화한다.
   - `method`, `path`, `auth`, `query/path/body`, `response`, `error`를 분리한다.
3. 필드 제약을 추출한다.
   - required 여부, enum, 길이/범위 제약, nullable 여부를 기록한다.
4. 화면 계약 관점으로 재배치한다.
   - 프론트 액션에서 필요한 필드 묶음으로 정리한다.
5. 미정의 항목을 태깅한다.
   - 스펙 누락/모호 문구/도메인 경계 불명확 항목을 질문 목록으로 만든다.

## Output Contract

아래 5개 섹션을 항상 같은 순서로 출력한다.

1. `입력 범위`
2. `결정 사항`
3. `불일치 목록`
4. `사용자 확인 필요 항목`
5. `다음 에이전트 전달 요약(10줄 이내)`

## Artifact Path

- 기본 산출물: `spec-docs/working/api_spec_normalized.md`
- 템플릿: `references/normalization-template.md`

## Rules

- 문서에 없는 값은 생성하지 않는다.
- 문서 내 상충 구문은 둘 다 기록하고 우선순위 결정을 요청한다.
- 에러코드는 도메인별로 분리하여 기록한다.

