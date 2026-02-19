# Frontend-Backend API Contract Matrix (Draft v0.1)

- 기준일: 2026-02-19
- 정책: **Spec-First** (기본), 단 프론트 UX 유지 가치가 높으면 `Spec Update` 후보로 별도 분류
- 범위: `Threads`, `Chat`, `LostPets`, `Diary`
- 입력 소스:
  - 프론트: `aini-inu-frontend/src/services/api/*`, `aini-inu-frontend/src/hooks/*`, `aini-inu-frontend/src/app/around-me/*`
  - 스펙: `spec-docs/backend_spec_03_api.md`, `spec-docs/backend_spec_04_erd.md`, `spec-docs/backend_spec_05_ddl.md`

## Status 정의

- `Aligned`: 현재 프론트 호출과 스펙이 일치
- `Frontend Patch`: 스펙 유지, 프론트 호출/매핑 수정 필요
- `Spec Update`: 프론트 UX를 살리기 위해 스펙 신규/수정 필요
- `Decision Needed`: 정책 결정 필요 (BE/FE 합의 후 확정)

---

## Contract Matrix

| Domain | Screen/Route | User Action | Current Frontend Call | Spec Endpoint | Request Mapping | Response Mapping | Gap Type | Decision Status | Owner | Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| Thread | `/around-me` | 주변 마커/목록 조회 | `GET /api/v1/threads?lat={lat}&lng={lng}` | `GET /threads/map?latitude&longitude` 또는 `GET /threads` | `lat/lng` → `latitude/longitude`로 변환 필요 | 프론트는 `Thread[]`, 스펙은 `map:list` 또는 `slice.content` | Path mismatch | Frontend Patch | FE | Q-TH-001 |
| Thread | `/dashboard` | 모집글 미리보기 조회 | `GET /api/v1/threads` | `GET /threads` | page/size/sort 파라미터 누락 | 스펙은 `SliceResponse`, 프론트는 배열 가정 | Field mismatch | Frontend Patch | FE | Q-TH-002 |
| Thread | `/around-me` | 모집글 생성 | `POST /api/v1/threads` | `POST /threads` | 프론트 body(`lat,lng,name,image,author...`) ↔ 스펙 body(`walkDate,startTime,endTime,location,petIds...`) 불일치 | 응답 필드도 구조 차이(`location object`, `pets`) | Field mismatch | Frontend Patch | FE | Q-TH-003 |
| Thread | `/around-me` | 모집 참여 | `POST /api/v1/threads/{id}/join` | `POST /threads/{threadId}/apply` | `join` → `apply`, PET_OWNER는 `petIds` 필요 | 스펙 응답 `chatRoomId`를 프론트에서 미사용 | Path mismatch | Frontend Patch | FE | Q-TH-004 |
| Thread/Chat | `/around-me` | 참여 후 채팅 진입 | `POST /api/v1/chat/rooms { partnerId }` | `POST /threads/{threadId}/apply` 결과 `chatRoomId` 사용 | 프론트는 partnerId 기반 직접 DM 생성 시도 | 스펙은 apply/match 기반 채팅방 생성 | Missing endpoint | Decision Needed | BE+FE | Q-TH-005 |
| Thread | `/dashboard` | 핫스팟 조회 | `GET /api/v1/threads/hotspot?hours=3` | (해당 엔드포인트 없음) | 프론트 기능 유지 시 신규 스펙 필요 | - | Missing endpoint | Decision Needed | BE | Q-TH-006 |
| Chat | `/chat` | 채팅방 목록 조회 | `GET /api/v1/chat/rooms` | `GET /chat-rooms` | 경로 `/chat/rooms` ↔ `/chat-rooms` | 프론트 `ChatRoom[]` 가정, 스펙은 `PageResponse.content` | Path mismatch | Frontend Patch | FE | Q-CH-001 |
| Chat | `/chat/[id]` | 채팅방 상세 조회 | (별도 상세 호출 없음) `getRooms()` 후 클라이언트 find | `GET /chat-rooms/{chatRoomId}` | 상세 전용 API 미사용 | 프론트 `partner` 중심 모델, 스펙은 `participants[]`/`thread` | Missing UX action | Frontend Patch | FE | Q-CH-002 |
| Chat | `/chat/[id]` | 메시지 목록 조회 | `GET /api/v1/chat/rooms/{id}/messages` | `GET /chat-rooms/{chatRoomId}/messages` | 경로 불일치 + cursor 파라미터 미사용 | 프론트 배열 가정, 스펙은 `{content,nextCursor,hasMore}` | Path mismatch | Frontend Patch | FE | Q-CH-003 |
| Chat | `/chat/[id]` | 메시지 전송 | `POST /api/v1/chat/rooms/{id}/messages` | `POST /chat-rooms/{chatRoomId}/messages` | 경로 불일치 | 응답 필드(`content`,`sentAt`)를 프론트 모델(`text`,`timestamp`)로 변환 필요 | Path mismatch | Frontend Patch | FE | Q-CH-004 |
| Chat | `/around-me` | 이웃과 바로 채팅 | `POST /api/v1/chat/rooms {partnerId}` | (직접 생성 스펙 없음) | 스펙상 채팅방 생성 트리거는 `apply` 또는 `lost-pets/{id}/match` | - | Missing endpoint | Decision Needed | BE+FE | Q-CH-005 |
| LostPets | `/around-me` (LOST) | 실종 신고 등록 | 현재 `POST /api/v1/threads`(isEmergency) 재사용 | `POST /lost-pets` | 도메인 자체 불일치(threads vs lost-pets) | 스펙은 `lostPetId/status/similarSightingsCount` 반환 | Missing endpoint | Frontend Patch | FE | Q-LP-001 |
| LostPets | `/around-me` (FOUND) | 발견 제보 등록 | 현재 `POST /api/v1/threads`(isEmergency) 재사용 | `POST /sightings` | 도메인 자체 불일치(threads vs sightings) | 스펙은 `sightingId/potentialMatchCount` 반환 | Missing endpoint | Frontend Patch | FE | Q-LP-002 |
| LostPets/AI | `/around-me` | 사진 AI 분석 | `POST http://localhost:8080/api/v1/pets/analyze` | (해당 엔드포인트 없음) | 절대 URL + 비스펙 API | 반환 스키마도 미정 | Missing endpoint | Decision Needed | BE | Q-LP-003 |
| LostPets | `/around-me` | 유사 후보 조회 | 현재 mock 배열 생성 | `GET /lost-pets/{lostPetId}/similar-sightings` | LOST 흐름은 스펙 API로 대체 가능 | 프론트는 단순 `matchRate`, 스펙은 `similarityScore(total/image/location/time)` | Missing UX action | Frontend Patch | FE | Q-LP-004 |
| LostPets | `/around-me` | “이 아이예요” 후 대화 시작 | 현재 `POST /api/v1/chat/rooms` | `POST /lost-pets/{lostPetId}/match` | `match` 호출 후 `chatRoomId`로 이동 필요 | 스펙 응답 `chatRoomId` 제공 | Missing endpoint | Frontend Patch | FE | Q-LP-005 |
| Diary | `/feed` | 팔로잉 일기 조회 | `GET /api/v1/walk-diaries/following` | (스펙 없음) | 신규 Diary 스펙 필요 | - | Missing endpoint | Spec Update | BE | Q-DI-001 |
| Diary | `/profile`, `/dashboard` | 멤버 일기 조회 | `GET /api/v1/walk-diaries?memberId=` | (스펙 없음) | 신규 Diary 스펙 필요 | - | Missing endpoint | Spec Update | BE | Q-DI-002 |
| Diary | `DiaryModal` | 일기 저장 | `POST /api/v1/walk-diaries/{id}` | (스펙 없음) | `{id}` 의미(일기ID/스레드ID) 미정 | draft/public/tags/photos 모델 미정 | Missing endpoint | Decision Needed | BE+FE | Q-DI-003 |

---

## 미사용(하지만 스펙에 존재) API 후보

- Threads: `DELETE /threads/{threadId}/apply`, `GET /threads/check-duplicate`
- Chat: `DELETE /chat-rooms/{chatRoomId}/leave`, `POST/GET/DELETE /chat-rooms/{chatRoomId}/walk-confirm`, `POST /chat-rooms/{chatRoomId}/reviews`, `GET /chat-rooms/{chatRoomId}/reviews/me`
- LostPets: `GET /lost-pets/mine`, `PATCH /lost-pets/{lostPetId}/close`, `GET /sightings/mine`, `GET /sightings/{sightingId}`, `DELETE /sightings/{sightingId}`

---

## 우선순위 실행안 (초안)

1. **P0 (즉시)**: 경로/응답 형태가 명확한 `Frontend Patch`부터 정리  
   - `/chat/rooms` → `/chat-rooms` 계열  
   - `/threads/{id}/join` → `/threads/{id}/apply`  
   - `slice/content` 응답 어댑터 추가
2. **P1 (정책 결정)**: `Decision Needed` 항목 확정  
   - 직접 DM 허용 여부  
   - `threads/hotspot` 유지 여부  
   - `pets/analyze` API 신설 여부
3. **P2 (신규 스펙)**: Diary API/ERD/DDL 신설 및 계약 확정  
   - `Q-DI-001~003` 우선 결정 후 문서 반영

---

## 사용자 확인 필요 항목 (답변 요청)

1. `Q-TH-005`, `Q-CH-005`: 스펙 외 **직접 DM 생성 API**를 허용할지, 아니면 `apply/match` 기반만 허용할지?
2. `Q-TH-006`: 대시보드 핫스팟(`threads/hotspot`)을 **신규 스펙으로 추가**할지, 아니면 프론트 기능을 제거/대체할지?
3. `Q-LP-003`: `POST /pets/analyze`를 백엔드 정식 API로 채택할지? 채택 시 입력/출력 스키마 확정 필요.
4. `Q-DI-003`: `POST /walk-diaries/{id}`의 `{id}`를 **threadId**로 쓸지 **diaryId**로 분리할지? (현재 스펙 문서엔 diary 엔티티 자체가 없음)
5. `Q-LP-004`: FOUND 제보자 화면에서도 유사 매칭 리스트를 보여줄지? 필요하면 `sighting -> lost-pets` 조회 API 신규 정의 필요.

