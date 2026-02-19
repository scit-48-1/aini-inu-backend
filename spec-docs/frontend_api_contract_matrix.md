# Frontend-Backend API Contract Matrix (Master Draft v0.2)

- 기준일: 2026-02-19
- 정책: **Spec-First**
- 범위: Auth, Member, Pet, Threads, Chat, LostPets, Diary, Community, Notifications, Uploads, Block
- 원칙:
  - 프론트 UI/UX는 최대한 유지
  - 백엔드 스펙과 계약 불일치는 `Frontend Patch` / `Spec Update` / `Phase-2` / `Excluded`로 명시

## 1) 결정 고정(Decision Fixed)

| ID | 결정 항목 | 확정 값 | 상태 |
|---|---|---|---|
| D-001 | 직접 DM 생성 API | 미허용 (`apply/match` 기반만 허용) | Fixed |
| D-002 | 핫스팟 API | `GET /threads/hotspot` 신규 추가 | Fixed |
| D-003 | 분석 프리뷰 API | `POST /lost-pets/analyze` 신규 추가 | Fixed |
| D-004 | Diary 식별자 | `diaryId` 독립 엔티티 | Fixed |
| D-005 | FOUND 유사매칭 | `GET /sightings/{sightingId}/similar-lost-pets` 신규 추가 | Fixed |
| D-006 | Auth MVP | 이메일 로그인 기본 + 소셜은 유지(후순위) | Fixed |
| D-007 | 가입/프로필 | `POST /members/signup` + `POST /members/profile` 분리 | Fixed |
| D-008 | 후기 API | `POST /chat-rooms/{chatRoomId}/reviews`로 통일 | Fixed |
| D-009 | 팔로워/팔로잉 | 목록 API 신규 추가 (`SliceResponse`) | Fixed |
| D-010 | Walk 통계 | `GET /members/me/stats/walk` (`number[]`) 신규 추가 | Fixed |
| D-011 | Stories | Diary 기반 `GET /stories` 신규 추가 | Fixed |
| D-012 | Block 기능 | MVP 범위 제외 (`Excluded by Product Decision`) | Fixed |

---

## 2) 프론트 호출 기준 불일치 (FE → Spec)

| Domain | Screen/Route | User Action | Current Frontend Call | Target Contract | Gap Type | Status | Notes |
|---|---|---|---|---|---|---|---|
| Auth | `/login` | 이메일 로그인 | `POST /auth/login` | **신규** `POST /auth/login` | Missing endpoint | Spec Update | MVP 핵심 |
| Auth | `/signup` | 회원가입 | `POST /members/signup` | **신규** `POST /members/signup` | Missing endpoint | Spec Update | 최소 필수 필드 |
| Auth | `/signup` | 이메일 인증코드 전송 | `POST /auth/email/send` | Phase-2 | Missing endpoint | Frontend Patch | MVP에서 UI 제거/비활성 |
| Auth | `/signup` | 이메일 인증코드 검증 | `POST /auth/email/verify` | Phase-2 | Missing endpoint | Frontend Patch | MVP에서 UI 제거/비활성 |
| Member/Pet | 공통 | 내 반려견 목록 | `GET /members/me/dogs` | `GET /pets` | Path mismatch | Frontend Patch | 스펙 경로로 통일 |
| Member/Pet | 프로필 | 타인 반려견 목록 | `GET /members/{id}/dogs` | **신규** `GET /members/{memberId}/pets` | Missing endpoint | Spec Update | 프로필 UX 유지 |
| Member/Pet | 프로필 | 반려견 등록 | `POST /members/me/dogs` | `POST /pets` | Path mismatch | Frontend Patch | |
| Member/Pet | 프로필 | 반려견 삭제 | `DELETE /members/me/dogs/{id}` | `DELETE /pets/{petId}` | Path mismatch | Frontend Patch | |
| Member | 프로필 | 팔로워 목록 | `GET /members/me/followers` | **신규** `GET /members/me/followers` | Missing endpoint | Spec Update | SliceResponse |
| Member | 프로필 | 팔로잉 목록 | `GET /members/me/following` | **신규** `GET /members/me/following` | Missing endpoint | Spec Update | SliceResponse |
| Member | 대시보드 | 산책 통계 | `GET /members/me/stats/walk` | **신규** `GET /members/me/stats/walk` | Missing endpoint | Spec Update | `number[]` |
| Member/Chat | 대시보드 | 후기 작성 | `POST /members/{partnerId}/reviews` | `POST /chat-rooms/{chatRoomId}/reviews` | Contract mismatch | Frontend Patch | 채팅방 기반으로 변경 |
| Threads | `/around-me` | 지도 스레드 조회 | `GET /threads?lat&lng` | `GET /threads/map?latitude&longitude` | Field mismatch | Frontend Patch | 파라미터명 변환 |
| Threads | `/around-me` | 스레드 참여 | `POST /threads/{id}/join` | `POST /threads/{threadId}/apply` | Path mismatch | Frontend Patch | `petIds` 처리 필요 |
| Threads | `/dashboard` | 핫스팟 조회 | `GET /threads/hotspot?hours` | **신규** `GET /threads/hotspot` | Missing endpoint | Spec Update | Top1 `{region,count}` |
| Threads | `/around-me` | 스레드 생성 | `POST /threads` (프론트 body) | `POST /threads` (스펙 body) | Field mismatch | Frontend Patch | request schema 정렬 |
| Chat | `/chat` | 채팅방 목록 | `GET /chat/rooms` | `GET /chat-rooms` | Path mismatch | Frontend Patch | PageResponse 매핑 |
| Chat | `/chat/[id]` | 메시지 목록 | `GET /chat/rooms/{id}/messages` | `GET /chat-rooms/{id}/messages` | Path mismatch | Frontend Patch | cursor pagination 반영 |
| Chat | `/chat/[id]` | 메시지 전송 | `POST /chat/rooms/{id}/messages` | `POST /chat-rooms/{id}/messages` | Path mismatch | Frontend Patch | |
| Chat | `/around-me` | 이웃과 바로 대화 시작 | `POST /chat/rooms {partnerId}` | 사용 금지 (`apply/match` 경유) | Policy mismatch | Frontend Patch | D-001 |
| LostPets | `/around-me` LOST | 실종 등록 | (현재) `POST /threads` emergency 재사용 | `POST /lost-pets` | Domain mismatch | Frontend Patch | 도메인 분리 필수 |
| LostPets | `/around-me` FOUND | 제보 등록 | (현재) `POST /threads` emergency 재사용 | `POST /sightings` | Domain mismatch | Frontend Patch | |
| LostPets | `/around-me` | 사진 사전 분석 | `POST http://localhost:8080/api/v1/pets/analyze` | **신규** `POST /lost-pets/analyze` | Endpoint mismatch | Spec Update + Frontend Patch | 절대 URL 제거 |
| LostPets | `/around-me` FOUND | 유사 실종 목록 | (mock 생성) | **신규** `GET /sightings/{sightingId}/similar-lost-pets` | Missing endpoint | Spec Update | D-005 |
| LostPets | `/around-me` LOST | 유사 제보 목록 | (mock 생성) | `GET /lost-pets/{lostPetId}/similar-sightings` | Missing UX action | Frontend Patch | 스펙 API 연동 |
| LostPets | `/around-me` | “이 아이예요” 후 채팅 | (현재) 직접 chat room 생성 | `POST /lost-pets/{lostPetId}/match` | Flow mismatch | Frontend Patch | `chatRoomId` 사용 |
| Diary | `/feed` | 팔로잉 일기 목록 | `GET /walk-diaries/following` | **신규 Diary API** | Missing endpoint | Spec Update | |
| Diary | `/profile`,`/dashboard` | 멤버 일기 목록 | `GET /walk-diaries?memberId` | **신규 Diary API** | Missing endpoint | Spec Update | |
| Diary | `DiaryModal` | 일기 저장 | `POST /walk-diaries/{id}` | **신규 Diary API** | Missing endpoint | Spec Update | `diaryId` 기준 |
| Community | `/feed` | 게시물 목록 | `GET /posts?memberId&location` | `GET /posts` + 필터 확장 | Field mismatch | Spec Update + Frontend Patch | member/location 쿼리 추가 권장 |
| Community | `/feed` | 게시물 생성 | `POST /posts` (`caption`,`images`,`location`) | `POST /posts` (`content`,`imageUrls`) | Field mismatch | Frontend Patch | DTO 정렬 |
| Community | `/feed` | 게시물 수정 | `PUT /posts/{id}` | `PATCH /posts/{postId}` | Method mismatch | Frontend Patch | |
| Community | `/feed` | 댓글 목록 조회 | `GET /posts/{id}/comments` | **신규** `GET /posts/{postId}/comments` | Missing endpoint | Spec Update | |
| Community | `/feed` | 스토리 조회 | `GET /stories` | **신규** `GET /stories` | Missing endpoint | Spec Update | Diary 기반 |

---

## 3) 백엔드 스펙 기준 미연동 (Spec → FE)

| Domain | Spec Endpoint | FE 상태 | 분류 | 비고 |
|---|---|---|---|---|
| Auth | `POST /auth/refresh` | 미연동 | MVP Included | 토큰 재발급 로직 추가 필요 |
| Auth | `POST /auth/logout` | 미연동 | MVP Included | 로그아웃 시 리프레시 폐기 |
| Threads | `DELETE /threads/{threadId}/apply` | 미연동 | Phase-2 | 참가 취소 UX 추가 |
| Threads | `GET /threads/check-duplicate` | 미연동 | Phase-2 | 모집 생성 UX 고도화 시 반영 |
| Chat | `GET /chat-rooms/{chatRoomId}` | 간접/부분 사용 | Frontend Patch | 상세 API 직접 사용으로 전환 |
| Chat | `DELETE /chat-rooms/{chatRoomId}/leave` | 미연동 | Phase-2 | 채팅방 나가기 버튼 필요 |
| Chat | `POST/GET/DELETE /chat-rooms/{chatRoomId}/walk-confirm` | 미연동 | Phase-2 | 1:1 확정 UX 미구성 |
| Chat | `GET /chat-rooms/{chatRoomId}/reviews/me` | 미연동 | Phase-2 | 후기 이력 UI 미구성 |
| LostPets | `GET /lost-pets/mine` | 미연동 | Phase-2 | 마이 신고 목록 페이지 필요 |
| LostPets | `PATCH /lost-pets/{lostPetId}/close` | 미연동 | Phase-2 | 신고 종료 UX 필요 |
| LostPets | `GET /sightings/mine` | 미연동 | Phase-2 | 제보 목록 페이지 필요 |
| LostPets | `GET /sightings/{sightingId}` | 미연동 | Phase-2 | 제보 상세 화면 필요 |
| LostPets | `DELETE /sightings/{sightingId}` | 미연동 | Phase-2 | 제보 삭제 UX 필요 |
| Notifications | `GET /notifications` 외 4개 | 미연동 | Phase-2 | 알림센터 UI 미구성 |
| Uploads | `POST /images/presigned-url` | 미연동 | MVP Included | Post/Diary/LostPets 업로드 표준화 필요 |
| Block | `POST/GET/DELETE /blocks*` | 미연동 | Excluded | Product 결정으로 MVP 제외 |

---

## 4) Diary 신규 계약(초안 범위)

- 엔드포인트 신규 추가 대상
  - `POST /walk-diaries`
  - `GET /walk-diaries`
  - `GET /walk-diaries/{diaryId}`
  - `PATCH /walk-diaries/{diaryId}`
  - `DELETE /walk-diaries/{diaryId}`
  - `GET /walk-diaries/following`
- 핵심 필드(초안)
  - `id(diaryId)`, `author`, `title`, `content`, `photoUrls`, `walkDate`, `location`, `isPublic`, `tags`, `createdAt`, `updatedAt`

---

## 5) 우선순위 실행 순서

1. **P0 (계약 고정 + 스펙 추가)**  
   Auth(email MVP), Member 확장(follow/stats), Threads hotspot, LostPets analyze/found-similar, Diary, Community stories/comments
2. **P1 (프론트 경로/메서드 정렬)**  
   `join→apply`, `chat/rooms→chat-rooms`, `PUT→PATCH`, DTO 매핑 정렬
3. **P2 (미연동 기능 반영)**  
   walk-confirm, leave, mine/list 계열, notifications, duplicate-check
4. **제외**  
   Block 기능은 MVP 제외 상태 유지

