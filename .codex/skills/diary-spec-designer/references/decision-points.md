# Diary Decision Points

## Domain Rules
- 다이어리 작성 주체: 작성자 본인만 가능한가
- 스레드-다이어리 관계: 1:N vs N:N
- 공개 범위: 전체/친구/참여자/비공개

## Data Rules
- 사진 최대 개수/용량
- 태그/감정/날씨 등 메타데이터 필수 여부
- 수정 가능 기간 제한 여부

## Lifecycle Rules
- soft delete만 허용하는가
- 신고/블라인드 정책 연동 여부
- 알림 발송 트리거 정의 여부

## API Rules
- 목록 기본 정렬(최신/인기)
- 페이지네이션 방식(cursor/page)
- 권한 실패 시 에러코드 체계

