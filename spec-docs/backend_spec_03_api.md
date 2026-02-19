# 3. API 명세서

## 3.1 프로젝트 정보

- **프로젝트명**: 아이니이누 (Aini Inu)
- **API 버전**: v1
- **Base URL**: `https://api.aini-inu.com/api/v1`
- **인증 방식**: JWT Bearer Token
- **인증 구현**: Spring Interceptor + ArgumentResolver (Spring Security 미사용)

### 인증 처리 방식

| 구분 | 설명 |
|------|------|
| **인증 필요 API** | `Authorization: Bearer {accessToken}` 헤더 필수 |
| **인증 불필요 API** | `@Public` 어노테이션으로 표시 (로그인, 토큰 갱신 등) |
| **인증 선택적 API** | 토큰 없이도 접근 가능, 토큰 있으면 추가 정보 제공 |

**인증 불필요 API 목록**:
- `POST /auth/login` - 이메일 로그인 (MVP)
- `POST /auth/login/{provider}` - 소셜 로그인
- `POST /members/signup` - 회원 가입 (MVP)
- `POST /auth/refresh` - 토큰 갱신
- `GET /breeds` - 견종 목록 조회
- `GET /personalities` - 성향 카테고리 목록 조회
- `GET /walking-styles` - 산책 스타일 목록 조회
- `GET /member-personality-types` - 견주 성향 카테고리 목록 조회

## 3.1.1 공통 응답 포맷

모든 API는 아래의 `ApiResponse<T>` 래퍼로 응답합니다.

- 성공 응답은 HTTP `200 OK`로 통일하며, 본문 `status`도 `200`으로 반환합니다.

**Success**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "example": "..."
  }
}
```

**Error**
```json
{
  "success": false,
  "status": 400,
  "data": null,
  "errorCode": "C001",
  "message": "잘못된 입력값입니다"
}
```

- `errorCode`는 `{PREFIX}{NUMBER}` 형식을 사용합니다. (예: C001, M001, P001, T001, CH001, CO001, L001, N001)

### 페이지네이션 응답 (PageResponse / SliceResponse)

페이지네이션이 필요한 목록 API는 아래 중 하나로 응답합니다.  
`pageNumber`는 **0부터 시작**합니다. (Spring `Page.getNumber()` 기준)

- **PageResponse**: 총 개수/총 페이지가 필요한 일반 페이지네이션
- **SliceResponse**: 무한 스크롤용(Count 쿼리 없음) → `hasNext`로 다음 페이지 여부만 제공

**PageResponse 예시**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
}
```

**SliceResponse 예시**

```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": false,
    "hasNext": true
  }
}
```

## 3.2 인증 (Authentication)

### 이메일 로그인 (MVP)

**Endpoint**: `POST /auth/login`

**설명**: 이메일/비밀번호로 JWT 토큰을 발급받습니다.

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Abcd1234!"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "memberId": 1,
    "nickname": "건홍이네"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | C001 | 입력값 검증 실패 |
| 401 | C104 | 이메일 또는 비밀번호가 올바르지 않음 |
| 403 | M005 | 정지된 회원 |

---

### 소셜 로그인

**Endpoint**: `POST /auth/login/{provider}`

**설명**: 소셜 로그인을 통해 JWT 토큰을 발급받습니다.

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| provider | string | 소셜 로그인 제공자 (naver, kakao, google) |

**Request Body**
```json
{
  "accessToken": "소셜 로그인 액세스 토큰"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "isNewMember": true,
    "memberId": 1
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | C001 | 지원하지 않는 소셜 로그인 제공자 |
| 401 | M004 | 유효하지 않은 소셜 토큰 |
| 403 | M005 | 정지된 회원 |

---

### 토큰 갱신

**Endpoint**: `POST /auth/refresh`

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

### 로그아웃

**Endpoint**: `POST /auth/logout`

**설명**: 리프레시 토큰을 폐기하여 세션을 종료합니다.

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 401 | C101 | 인증 실패 |
| 401 | C102 | 유효하지 않은 리프레시 토큰 |

---

## 3.3 회원 (Members)

### 회원 가입 (계정 생성, MVP)

**Endpoint**: `POST /members/signup`

**설명**:
- 이메일 기반 계정을 생성합니다.
- MVP에서는 최소 필드만 입력받고, 상세 프로필은 `POST /members/profile`에서 설정합니다.

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Abcd1234!",
  "nickname": "건홍이네",
  "memberType": "NON_PET_OWNER"
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | O | 이메일 (고유) |
| password | string | O | 비밀번호 (최소 8자) |
| nickname | string | O | 닉네임 (최대 10자, 고유) |
| memberType | string | O | 회원 유형 (PET_OWNER, NON_PET_OWNER) |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "건홍이네",
    "memberType": "NON_PET_OWNER",
    "createdAt": "2026-01-26T10:00:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | C001 | 입력값 검증 실패 |
| 409 | M003 | 이미 사용 중인 닉네임 |
| 409 | M007 | 이미 가입된 이메일 |

---

### 회원 가입 완료 (프로필 설정)

**Endpoint**: `POST /members/profile`

**설명**: 회원가입 후 프로필 정보를 설정/보완합니다. (최초 가입 시 `memberType`은 `NON_PET_OWNER` 기본값이며, 반려견 등록 시 `PET_OWNER`로 자동 전환됩니다.)

**Request Body**
```json
{
  "nickname": "건홍이네",
  "profileImageUrl": "https://s3.../profile.jpg",
  "linkedNickname": "몽이아빠",
  "age": 29,
  "gender": "MALE",
  "mbti": "INTJ",
  "personality": "차분하고 배려심이 많아요",
  "selfIntroduction": "한강 근처에서 자주 산책해요!",
  "personalityTypeIds": [1, 3]
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| nickname | string | O | 닉네임 (최대 10자) |
| profileImageUrl | string | X | 프로필 이미지 URL |
| linkedNickname | string | X | 애견 연계 닉네임 (예: 몽이아빠) |
| age | integer | X | 나이 |
| gender | string | X | 성별 (MALE, FEMALE, UNKNOWN) |
| mbti | string | X | MBTI |
| personality | string | X | 성격 |
| selfIntroduction | string | X | 자기소개 |
| personalityTypeIds | array | X | 견주 성향 카테고리 ID 목록 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "건홍이네",
    "memberType": "NON_PET_OWNER",
    "profileImageUrl": "https://s3.../profile.jpg",
    "linkedNickname": null,
    "age": 29,
    "gender": "MALE",
    "mbti": "INTJ",
    "personality": "차분하고 배려심이 많아요",
    "selfIntroduction": "한강 근처에서 자주 산책해요!",
    "personalityTypes": [
      {"id": 1, "name": "동네친구", "code": "LOCAL_FRIEND"},
      {"id": 3, "name": "랜선집사", "code": "ONLINE_PET_LOVER"}
    ],
    "mannerTemperature": 5.0,
    "status": "ACTIVE",
    "createdAt": "2026-01-26T10:00:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | M002 | 닉네임이 유효하지 않음 (길이 초과 등) |
| 409 | M003 | 이미 사용 중인 닉네임 |

---

### 내 프로필 조회

**Endpoint**: `GET /members/me`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "건홍이네",
    "memberType": "PET_OWNER",
    "profileImageUrl": "https://s3.../profile.jpg",
    "linkedNickname": "몽이아빠",
    "mannerTemperature": 5.0,
    "status": "ACTIVE",
    "age": 29,
    "gender": "MALE",
    "mbti": "INTJ",
    "personality": "차분하고 배려심이 많아요",
    "selfIntroduction": "한강 근처에서 자주 산책해요!",
    "personalityTypes": [
      {"id": 1, "name": "동네친구", "code": "LOCAL_FRIEND"}
    ],
    "isVerified": true,
    "createdAt": "2026-01-26T10:00:00+09:00",
    "pets": [
      {
        "id": 1,
        "name": "몽이",
        "breed": "포메라니안",
        "photoUrl": "https://s3.../pet.jpg",
        "isMain": true
      }
    ]
  }
}
```

---

### 프로필 수정

**Endpoint**: `PATCH /members/me`

**Request Body**
```json
{
  "nickname": "새닉네임",
  "profileImageUrl": "https://s3.../new-profile.jpg",
  "linkedNickname": "몽이엄마",
  "selfIntroduction": "주말마다 산책해요!",
  "personalityTypeIds": [1, 4]
}
```

**Response (200 OK)**: 수정된 프로필 정보 반환

---

### 다른 회원 프로필 조회

**Endpoint**: `GET /members/{memberId}`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 2,
    "nickname": "뭉치맘",
    "memberType": "PET_OWNER",
    "profileImageUrl": "https://s3.../profile.jpg",
    "linkedNickname": "뭉치엄마",
    "mannerTemperature": 7.5,
    "age": 31,
    "gender": "FEMALE",
    "mbti": "ENFP",
    "personality": "활발하고 친화적이에요",
    "selfIntroduction": "뭉치랑 동네 친구 구해요!",
    "personalityTypes": [
      {"id": 1, "name": "동네친구", "code": "LOCAL_FRIEND"}
    ],
    "pets": [
      {
        "id": 5,
        "name": "뭉치",
        "breed": "말티즈",
        "photoUrl": "https://s3.../pet.jpg",
        "isMain": true
      }
    ]
  }
}
```

---

### 다른 회원 반려견 목록 조회

**Endpoint**: `GET /members/{memberId}/pets`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 5,
      "name": "뭉치",
      "breed": {"id": 13, "name": "말티즈"},
      "age": 2,
      "gender": "MALE",
      "size": "SMALL",
      "photoUrl": "https://s3.../pet.jpg",
      "isMain": true,
      "isCertified": true
    }
  ]
}
```

---

### 내 팔로워 목록 조회

**Endpoint**: `GET /members/me/followers`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 10,
        "nickname": "산책메이트",
        "profileImageUrl": "https://s3.../profile.jpg",
        "mannerTemperature": 6.5,
        "followedAt": "2026-01-24T08:00:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": false,
    "hasNext": true
  }
}
```

---

### 내 팔로잉 목록 조회

**Endpoint**: `GET /members/me/following`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 11,
        "nickname": "몽이친구",
        "profileImageUrl": "https://s3.../profile2.jpg",
        "mannerTemperature": 7.1,
        "followedAt": "2026-01-20T11:00:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": true,
    "hasNext": false
  }
}
```

---

### 내 산책 활동 통계 조회

**Endpoint**: `GET /members/me/stats/walk`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| weeks | integer | X | 18 | 통계 주차(칸) 수 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [0, 1, 0, 2, 1, 0, 0, 3]
}
```

---

### 견주 성향 카테고리 목록 조회

**Endpoint**: `GET /member-personality-types`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {"id": 1, "name": "동네친구", "code": "LOCAL_FRIEND"},
    {"id": 2, "name": "반려견정보공유", "code": "PET_INFO_SHARING"},
    {"id": 3, "name": "랜선집사", "code": "ONLINE_PET_LOVER"},
    {"id": 4, "name": "강아지만좋아함", "code": "DOG_LOVER_ONLY"}
  ]
}
```

---

## 3.4 반려견 (Pets)

### 반려견 등록

**Endpoint**: `POST /pets`

**설명**:
- 반려견을 등록합니다.
- **첫 반려견 등록 시** 회원의 `memberType`이 `PET_OWNER`로 **자동 전환**됩니다.

**Request Body**
```json
{
  "name": "몽이",
  "breedId": 15,
  "age": 3,
  "gender": "MALE",
  "size": "SMALL",
  "mbti": "ENFP",
  "isNeutered": true,
  "photoUrl": "https://s3.../pet.jpg",
  "isMain": true,
  "certificationNumber": "410123456789012",
  "walkingStyles": ["ENERGY_BURST", "SNIFF_EXPLORER"],
  "personalityIds": [1, 3, 5]
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | O | 반려견 이름 (최대 10자) |
| breedId | integer | O | 견종 ID |
| age | integer | O | 나이 |
| gender | string | O | 성별 (MALE, FEMALE) |
| size | string | O | 크기 (SMALL, MEDIUM, LARGE) |
| mbti | string | X | MBTI |
| isNeutered | boolean | O | 중성화 여부 |
| photoUrl | string | O | 사진 URL |
| isMain | boolean | O | 메인 반려견 여부 |
| certificationNumber | string | X | 동물등록번호 (15자리) |
| walkingStyles | array | X | 산책 스타일 코드 목록 |
| personalityIds | array | X | 성향 카테고리 ID 목록 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1,
    "name": "몽이",
    "breed": {
      "id": 15,
      "name": "포메라니안"
    },
    "age": 3,
    "gender": "MALE",
    "size": "SMALL",
    "mbti": "ENFP",
    "isNeutered": true,
    "photoUrl": "https://s3.../pet.jpg",
    "isMain": true,
    "isCertified": true,
    "walkingStyles": ["ENERGY_BURST", "SNIFF_EXPLORER"],
    "personalities": [
      {"id": 1, "name": "에너지넘침"},
      {"id": 3, "name": "사람좋아함"},
      {"id": 5, "name": "친구구함"}
    ],
    "createdAt": "2026-01-26T10:00:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | P007 | 반려견 이름이 유효하지 않음 (공백 등) |
| 400 | P008 | 반려견 이름 길이 초과 (최대 10자) |
| 400 | P004 | 존재하지 않는 견종 |
| 400 | P002 | 등록 가능 마릿수(10) 초과 |
| 400 | P009 | 동물등록번호 형식 오류 (15자리 숫자가 아님) |
| 502 | P010 | 동물등록정보 조회 실패 (공공데이터 API 응답 없음) |
| 400 | P011 | 동물등록정보 불일치 (견종/소유자 정보가 일치하지 않음) |

---

### 동물등록번호 검증

반려견 등록 시 `certificationNumber`를 입력하면 농림축산식품부 공공데이터 API를 통해 검증을 수행합니다.

**검증 프로세스**

1. **형식 검증**: 15자리 숫자인지 확인 (실패 시 `P009`)
2. **공공 API 조회**: 농림축산식품부 동물등록정보 조회 서비스 호출 (실패 시 `P010`)
3. **정보 일치 검증**: 반환된 견종/소유자 정보와 요청 정보 비교 (불일치 시 `P011`)
4. **인증 완료**: 검증 성공 시 `isCertified = true`로 저장

**공공데이터 API 정보**

| 항목 | 내용 |
|------|------|
| API명 | 농림축산식품부_동물등록정보 조회서비스 |
| Open API | https://www.data.go.kr/data/15098931/openapi.do |
| 인증 방식 | API Key (serviceKey) |
| 응답 형식 | XML/JSON |

**응답 필드 매핑**

| 공공 API 필드 | 용도 |
|---------------|------|
| dogRegNo | 동물등록번호 확인 |
| kindNm | 견종 일치 검증 |
| sexNm | 성별 참고 (선택) |
| neuterYn | 중성화 여부 참고 (선택) |

**주의사항**
- `certificationNumber`는 선택 필드이며, 미입력 시 `isCertified = false`로 저장됩니다.
- 공공 API 장애 시 `P010` 에러를 반환하며, 사용자에게 나중에 다시 시도하도록 안내합니다.
- 검증 성공 시 프로필에 인증마크(✓)가 표시됩니다.

---

### 내 반려견 목록 조회

**Endpoint**: `GET /pets`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 1,
      "name": "몽이",
      "breed": {"id": 15, "name": "포메라니안"},
      "age": 3,
      "gender": "MALE",
      "size": "SMALL",
      "photoUrl": "https://s3.../pet.jpg",
      "isMain": true,
      "isCertified": true
    },
    {
      "id": 2,
      "name": "콩이",
      "breed": {"id": 15, "name": "포메라니안"},
      "age": 1,
      "gender": "FEMALE",
      "size": "SMALL",
      "photoUrl": "https://s3.../pet2.jpg",
      "isMain": false,
      "isCertified": false
    }
  ]
}
```

---

### 반려견 수정

**Endpoint**: `PATCH /pets/{petId}`

**Request Body**: 수정할 필드만 포함

**Response (200 OK)**: 수정된 반려견 정보 반환

---

### 반려견 삭제

**Endpoint**: `DELETE /pets/{petId}`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

---

### 메인 반려견 변경

**Endpoint**: `PATCH /pets/{petId}/main`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 2,
    "name": "콩이",
    "isMain": true
  }
}
```

---

### 견종 목록 조회

**Endpoint**: `GET /breeds`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| search | string | X | - | 견종 이름 검색 |
| size | string | X | - | 크기 필터 (SMALL, MEDIUM, LARGE) |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {"id": 1, "name": "골든 리트리버", "size": "LARGE"},
    {"id": 2, "name": "래브라도 리트리버", "size": "LARGE"},
    {"id": 15, "name": "포메라니안", "size": "SMALL"}
  ]
}
```

---

### 성향 카테고리 목록 조회

**Endpoint**: `GET /personalities`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {"id": 1, "name": "소심해요", "code": "SHY"},
    {"id": 2, "name": "에너지넘침", "code": "ENERGETIC"},
    {"id": 3, "name": "간식좋아함", "code": "TREAT_LOVER"},
    {"id": 4, "name": "사람좋아함", "code": "PEOPLE_LOVER"},
    {"id": 5, "name": "친구구함", "code": "SEEKING_FRIENDS"},
    {"id": 6, "name": "주인바라기", "code": "OWNER_FOCUSED"},
    {"id": 7, "name": "까칠해요", "code": "GRUMPY"}
  ]
}
```

---

### 산책 스타일 목록 조회

**Endpoint**: `GET /walking-styles`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {"id": 1, "name": "전력질주", "code": "ENERGY_BURST"},
    {"id": 2, "name": "냄새맡기집중", "code": "SNIFF_EXPLORER"},
    {"id": 3, "name": "공원벤치휴식형", "code": "BENCH_REST"},
    {"id": 4, "name": "느긋함", "code": "RELAXED"},
    {"id": 5, "name": "냄새탐정", "code": "SNIFF_DETECTIVE"},
    {"id": 6, "name": "무한동력", "code": "ENDLESS_ENERGY"},
    {"id": 7, "name": "저질체력", "code": "LOW_STAMINA"}
  ]
}
```

---

## 3.5 스레드 (Threads)

**필드 정의**
- `currentParticipants`: `GROUP`=현재 참가자 수(작성자 포함), `INDIVIDUAL`=해당 스레드에서 생성된 활성 1:1 채팅방 수(신청 수)

### 스레드 생성

**Endpoint**: `POST /threads`

**Request Body**
```json
{
  "title": "한강공원 같이 산책해요",
  "description": "오후에 한강공원에서 산책해요! 대형견 환영합니다~",
  "walkDate": "2026-01-27",
  "startTime": "2026-01-27T14:00:00+09:00",
  "endTime": "2026-01-27T16:00:00+09:00",
  "chatType": "GROUP",
  "maxParticipants": 5,
  "allowNonPetOwner": false,
  "isVisibleAlways": true,
  "location": {
    "placeName": "여의도 한강공원",
    "latitude": 37.5283,
    "longitude": 126.9328,
    "address": "서울특별시 영등포구 여의동로 330"
  },
  "petIds": [1, 2],
  "filters": [
    {"type": "SIZE", "values": ["MEDIUM", "LARGE"], "isRequired": true},
    {"type": "PERSONALITY", "values": [5], "isRequired": false}
  ]
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | string | O | 제목 (최대 30자) |
| description | string | O | 소개글 (최대 500자) |
| walkDate | string | O | 산책 날짜 (ISO 8601 date) |
| startTime | string | O | 시작 시간 (ISO 8601 datetime, KST) |
| endTime | string | O | 종료 시간 (ISO 8601 datetime, KST) |
| chatType | string | O | 채팅 방식 (INDIVIDUAL, GROUP) |
| maxParticipants | integer | X | 최대 참가자 수 (그룹만, 3~10) |
| allowNonPetOwner | boolean | O | 비애견인 참여 허용 여부 |
| isVisibleAlways | boolean | O | 항상 지도 표시 여부 |
| location | object | O | 장소 정보 |
| petIds | array | O | 참여 반려견 ID 목록 |
| filters | array | X | 참가 조건 필터 목록 |

**Filter Object**

| 필드 | 타입 | 설명 |
|------|------|------|
| type | string | 필터 유형 (SIZE, GENDER, NEUTERED, BREED, MBTI, PERSONALITY, WALKING_STYLE) |
| values | array | 필터 값 목록 |
| isRequired | boolean | 필수 조건 여부 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 100,
    "author": {
      "id": 1,
      "nickname": "몽이아빠",
      "profileImageUrl": "https://s3.../profile.jpg",
      "mannerTemperature": 5.0
    },
    "title": "한강공원 같이 산책해요",
    "description": "오후에 한강공원에서 산책해요! 대형견 환영합니다~",
    "walkDate": "2026-01-27",
    "startTime": "2026-01-27T14:00:00+09:00",
    "endTime": "2026-01-27T16:00:00+09:00",
    "chatType": "GROUP",
    "maxParticipants": 5,
    "currentParticipants": 1,
    "allowNonPetOwner": false,
    "isVisibleAlways": true,
    "status": "ACTIVE",
    "location": {
      "placeName": "여의도 한강공원",
      "latitude": 37.5283,
      "longitude": 126.9328,
      "address": "서울특별시 영등포구 여의동로 330"
    },
    "pets": [
      {"id": 1, "name": "몽이", "photoUrl": "https://s3.../pet.jpg"},
      {"id": 2, "name": "콩이", "photoUrl": "https://s3.../pet2.jpg"}
    ],
    "filters": [
      {"type": "SIZE", "values": ["MEDIUM", "LARGE"], "isRequired": true},
      {"type": "PERSONALITY", "values": [5], "isRequired": false}
    ],
    "createdAt": "2026-01-26T10:00:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | T008 | 유효하지 않은 산책 시간 (과거, 1주일 초과) |
| 400 | T009 | 종료 시간이 시작 시간보다 이전 |
| 400 | T010 | 참가자 수 범위 오류 (3~10) |
| 400 | T011 | 필수 필터 3개 초과 |
| 400 | T012 | 비애견인은 스레드 작성 불가 |
| 400 | T002 | 이미 활성 스레드가 존재함 |

---

### 스레드 목록 조회

**Endpoint**: `GET /threads`

**설명**:
- 스레드 목록을 조회합니다.
- 작성자의 **필수 조건(Hard Filter)** 을 충족하지 못하는 회원에게는 해당 스레드가 **노출되지 않으며**, 본 API 응답에도 포함되지 않습니다.
- 다견 가구 애견인의 필터 충족 판정은 **메인 반려견 기준**으로 수행합니다.
- `NON_PET_OWNER`는 `allowNonPetOwner=true`인 스레드만 조회할 수 있으며, 반려견 관련 필수 조건이 있는 스레드는 조회할 수 없습니다.
- 무한 스크롤을 위해 `SliceResponse` 형태로 응답합니다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 (최대 50) |
| sort | string | X | -startTime | 정렬 (startTime, distance, popularity) |
| latitude | decimal | X | - | 현재 위치 위도 (거리순 정렬 시 필수) |
| longitude | decimal | X | - | 현재 위치 경도 (거리순 정렬 시 필수) |
| radius | integer | X | 10 | 반경 (km) |
| startDate | string | X | - | 시작 날짜 필터 |
| endDate | string | X | - | 종료 날짜 필터 |
| startHour | integer | X | - | 시작 시간 필터 (0~23) |
| endHour | integer | X | - | 종료 시간 필터 (0~23) |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 100,
        "author": {
          "id": 1,
          "nickname": "몽이아빠",
          "profileImageUrl": "https://s3.../profile.jpg"
        },
        "title": "한강공원 같이 산책해요",
        "description": "오후에 한강공원에서 산책해요!",
        "walkDate": "2026-01-27",
        "startTime": "2026-01-27T14:00:00+09:00",
        "endTime": "2026-01-27T16:00:00+09:00",
        "chatType": "GROUP",
        "maxParticipants": 5,
        "currentParticipants": 3,
        "allowNonPetOwner": false,
        "location": {
          "placeName": "여의도 한강공원",
          "latitude": 37.5283,
          "longitude": 126.9328
        },
        "distance": 2.5,
        "mainPet": {
          "id": 1,
          "name": "몽이",
          "photoUrl": "https://s3.../pet.jpg"
        },
        "myFilterStatus": {
          "meetsRequired": true,
          "meetsPreferred": false,
          "unmatchedPreferred": ["친구구함"]
        }
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": false,
    "hasNext": true
  }
}
```

---

### 스레드 상세 조회

**Endpoint**: `GET /threads/{threadId}`

**설명**:
- 스레드 상세 정보를 조회합니다.
- 작성자의 **필수 조건(Hard Filter)** 을 충족하지 못하는 경우, 해당 스레드는 **미노출** 처리되며 `404 T001 (THREAD_NOT_FOUND)`로 응답합니다. (다견 가구는 메인 반려견 기준)

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 100,
    "author": {
      "id": 1,
      "nickname": "몽이아빠",
      "profileImageUrl": "https://s3.../profile.jpg",
      "mannerTemperature": 5.0
    },
    "title": "한강공원 같이 산책해요",
    "description": "오후에 한강공원에서 산책해요! 대형견 환영합니다~",
    "walkDate": "2026-01-27",
    "startTime": "2026-01-27T14:00:00+09:00",
    "endTime": "2026-01-27T16:00:00+09:00",
    "chatType": "GROUP",
    "maxParticipants": 5,
    "currentParticipants": 3,
    "allowNonPetOwner": false,
    "isVisibleAlways": true,
    "status": "ACTIVE",
    "location": {
      "placeName": "여의도 한강공원",
      "latitude": 37.5283,
      "longitude": 126.9328,
      "address": "서울특별시 영등포구 여의동로 330"
    },
    "pets": [
      {
        "id": 1,
        "name": "몽이",
        "breed": "포메라니안",
        "age": 3,
        "gender": "MALE",
        "photoUrl": "https://s3.../pet.jpg",
        "personalities": ["에너지넘침", "친구구함"]
      }
    ],
    "filters": {
      "required": [
        {"type": "SIZE", "values": ["MEDIUM", "LARGE"]}
      ],
      "preferred": [
        {"type": "PERSONALITY", "values": ["친구구함"]}
      ]
    },
    "myFilterStatus": {
      "meetsRequired": true,
      "meetsPreferred": false,
      "unmatchedPreferred": ["친구구함"]
    },
    "isApplied": false,
    "createdAt": "2026-01-26T10:00:00+09:00",
    "updatedAt": "2026-01-26T10:00:00+09:00"
  }
}
```

---

### 스레드 수정

**Endpoint**: `PATCH /threads/{threadId}`

**Request Body**: 수정할 필드만 포함

**Response (200 OK)**: 수정된 스레드 정보 반환

---

### 스레드 삭제

**Endpoint**: `DELETE /threads/{threadId}`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

---

### 산책 신청 (채팅방 입장)

**Endpoint**: `POST /threads/{threadId}/apply`

**설명**:
- 산책을 신청하고 채팅방에 입장합니다. (스레드 `chatType`에 따라 그룹 채팅방 또는 개별 1:1 채팅방이 생성/연결됩니다.)
- 작성자의 **필수 조건(Hard Filter)** 충족 여부는 신청자의 **메인 반려견 기준**으로 판정합니다.
- `PET_OWNER`의 `petIds`는 채팅방에서 표시/기록할 “참여 반려견”을 의미하며, 필수 필터 판정에는 사용하지 않습니다.

**Request Body**

- PET_OWNER: `petIds` 필수
- NON_PET_OWNER: Request Body 없이 신청 가능

```json
{
  "petIds": [1, 2]
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "chatRoomId": 500,
    "chatType": "GROUP",
    "threadId": 100,
    "joinedAt": "2026-01-26T10:30:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 403 | T006 | 비애견인 참여가 허용되지 않음 |
| 400 | T007 | 필수 필터 조건 미충족 (메인 반려견 기준) |
| 403 | M006 | 작성자에게 차단됨 |
| 409 | T013 | 이미 신청함 |
| 400 | T005 | 정원 초과 (그룹) |
| 400 | T004 | 스레드 종료됨 |

---

### 참가 취소 (채팅방 나가기)

**Endpoint**: `DELETE /threads/{threadId}/apply`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

---

### 지도용 스레드 조회

**Endpoint**: `GET /threads/map`

**설명**:
- 지도 마커 표시를 위한 스레드 목록을 조회합니다.
- 작성자의 **필수 조건(Hard Filter)** 을 충족하지 못하는 회원에게는 해당 스레드가 **노출되지 않으며**, 본 API 응답에도 포함되지 않습니다.
- 다견 가구 애견인의 필터 충족 판정은 **메인 반려견 기준**으로 수행합니다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| latitude | decimal | O | - | 중심 위도 |
| longitude | decimal | O | - | 중심 경도 |
| radius | integer | X | 5 | 반경 (km) |
| startHour | integer | X | - | 시작 시간 필터 (0~23) |
| endHour | integer | X | - | 종료 시간 필터 (0~23) |
| date | string | X | 오늘 | 날짜 필터 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 100,
      "title": "한강공원 같이 산책해요",
      "location": {
        "placeName": "여의도 한강공원",
        "latitude": 37.5283,
        "longitude": 126.9328
      },
      "startTime": "2026-01-27T14:00:00+09:00",
      "endTime": "2026-01-27T16:00:00+09:00",
      "chatType": "GROUP",
      "currentParticipants": 3,
      "maxParticipants": 5,
      "mainPet": {
        "name": "몽이",
        "photoUrl": "https://s3.../pet.jpg"
      }
    }
  ]
}
```

---

### 핫스팟 조회 (대시보드 추천)

**Endpoint**: `GET /threads/hotspot`

**설명**:
- 최근 지정 시간(`hours`) 내 활성 스레드를 지역 단위로 집계해 가장 활발한 지역 1개를 반환합니다.
- 대시보드 추천 배너용 API입니다.

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| hours | integer | X | 3 | 최근 집계 시간(시간 단위) |
| latitude | decimal | X | - | 기준 위도 (선택) |
| longitude | decimal | X | - | 기준 경도 (선택) |
| radius | integer | X | 10 | 집계 반경(km, 선택) |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "region": "성수동 서울숲",
    "count": 31
  }
}
```

---

### 중복 스레드 확인

**Endpoint**: `GET /threads/check-duplicate`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| placeName | string | O | 장소명 |
| latitude | decimal | O | 위도 |
| longitude | decimal | O | 경도 |
| date | string | O | 날짜 |
| startTime | string | O | 시작 시간 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "hasDuplicate": true,
    "nearbyThreads": [
      {
        "id": 99,
        "title": "여의도 산책 모집",
        "startTime": "2026-01-27T13:00:00+09:00",
        "endTime": "2026-01-27T15:00:00+09:00",
        "currentParticipants": 2,
        "author": {"nickname": "뭉치맘"}
      }
    ]
  }
}
```

---

## 3.6 채팅 (Chat)

**정책**
- 직접 DM 생성 API는 제공하지 않습니다.
- 채팅방은 `스레드 신청(apply)` 또는 `실종-제보 매칭(match)`을 통해서만 생성/연결됩니다.

### 채팅방 목록 조회

**Endpoint**: `GET /chat-rooms`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| status | string | X | ACTIVE | 상태 (ACTIVE, ARCHIVED) |
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 500,
        "roomPurpose": "WALK",
        "threadId": 100,
        "lostPetMatchId": null,
        "chatType": "GROUP",
        "status": "ACTIVE",
        "thread": {
          "id": 100,
          "title": "한강공원 같이 산책해요",
          "startTime": "2026-01-27T14:00:00+09:00",
          "location": {"placeName": "여의도 한강공원"}
        },
        "participantCount": 3,
        "lastMessage": {
          "content": "안녕하세요! 내일 뵙겠습니다~",
          "senderNickname": "뭉치맘",
          "sentAt": "2026-01-26T15:30:00+09:00"
        },
        "createdAt": "2026-01-26T10:30:00+09:00"
      },
      {
        "id": 600,
        "roomPurpose": "LOST_PET_MATCH",
        "threadId": null,
        "lostPetMatchId": 10,
        "chatType": "INDIVIDUAL",
        "status": "ACTIVE",
        "lostPetMatch": {
          "id": 10,
          "lostPetId": 50,
          "sightingId": 200
        },
        "participantCount": 2,
        "lastMessage": {
          "content": "사진 다시 한번 확인 부탁드려요",
          "senderNickname": "길가던사람",
          "sentAt": "2026-01-26T16:10:00+09:00"
        },
        "createdAt": "2026-01-26T16:00:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### 채팅방 상세 조회

**Endpoint**: `GET /chat-rooms/{chatRoomId}`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 500,
    "roomPurpose": "WALK",
    "threadId": 100,
    "lostPetMatchId": null,
    "chatType": "GROUP",
    "status": "ACTIVE",
    "thread": {
      "id": 100,
      "title": "한강공원 같이 산책해요",
      "description": "오후에 한강공원에서 산책해요! 대형견 환영합니다~",
      "walkDate": "2026-01-27",
      "startTime": "2026-01-27T14:00:00+09:00",
      "endTime": "2026-01-27T16:00:00+09:00",
      "location": {"placeName": "여의도 한강공원"}
    },
    "participants": [
      {
        "memberId": 1,
        "nickname": "몽이아빠",
        "memberType": "PET_OWNER",
        "profileImageUrl": "https://s3.../profile.jpg",
        "isAuthor": true,
        "joinedAt": "2026-01-26T10:00:00+09:00",
        "pets": [
          {"id": 1, "name": "몽이", "photoUrl": "https://s3.../pet.jpg"}
        ]
      },
      {
        "memberId": 2,
        "nickname": "뭉치맘",
        "memberType": "NON_PET_OWNER",
        "profileImageUrl": "https://s3.../profile2.jpg",
        "isAuthor": false,
        "joinedAt": "2026-01-26T10:30:00+09:00"
      }
    ],
    "chatEndTime": "2026-01-27T18:00:00+09:00",
    "createdAt": "2026-01-26T10:00:00+09:00"
  }
}
```

---

### 메시지 목록 조회

**Endpoint**: `GET /chat-rooms/{chatRoomId}/messages`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| cursor | string | X | - | 커서 (메시지 ID 기반) |
| size | integer | X | 50 | 페이지 크기 (최대 100) |
| direction | string | X | BEFORE | 방향 (BEFORE, AFTER) |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1001,
        "chatRoomId": 500,
        "messageType": "USER",
        "content": "안녕하세요! 반갑습니다~",
        "sender": {
          "memberId": 1,
          "nickname": "몽이아빠",
          "memberType": "PET_OWNER",
          "profileImageUrl": "https://s3.../profile.jpg",
          "mainPetPhotoUrl": "https://s3.../pet.jpg"
        },
        "sentAt": "2026-01-26T10:30:00+09:00"
      },
      {
        "id": 1002,
        "chatRoomId": 500,
        "messageType": "SYSTEM",
        "content": "뭉치맘님이 참여했습니다",
        "sender": null,
        "sentAt": "2026-01-26T10:35:00+09:00"
      }
    ],
    "nextCursor": "1000",
    "hasMore": true
  }
}
```

---

### 메시지 전송

**Endpoint**: `POST /chat-rooms/{chatRoomId}/messages`

**Request Body**
```json
{
  "content": "안녕하세요! 내일 뵙겠습니다~"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1003,
    "chatRoomId": 500,
    "messageType": "USER",
    "content": "안녕하세요! 내일 뵙겠습니다~",
    "sentAt": "2026-01-26T15:30:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | CH001 | 메시지 길이 초과 (500자) |
| 403 | CH002 | 아카이브된 채팅방 |
| 403 | CH003 | 채팅방 참여자가 아님 |

---

### 채팅방 나가기

**Endpoint**: `DELETE /chat-rooms/{chatRoomId}/leave`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

---

### 산책 참가 확정 (1:1 채팅 전용)

**Endpoint**: `POST /chat-rooms/{chatRoomId}/walk-confirm`

**설명**:
- 개별 대화(1:1) 산책 채팅방에서만 사용합니다.
- 작성자/신청자 각자가 "산책 확정"을 누르면 확정되며, **확정 상태는 채팅방 참여자에게만** 노출됩니다.
- 작성자는 같은 스레드(개별 대화)에서 **동시에 1개의 채팅방만** 산책 확정할 수 있습니다. (상대 확정 전에는 취소 후 다른 채팅방 확정 가능)

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "chatRoomId": 500,
    "myConfirmedAt": "2026-01-27T10:00:00+09:00",
    "otherConfirmedAt": null,
    "isWalkConfirmed": false
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 403 | CH003 | 채팅방 참여자가 아님 |
| 403 | CH004 | 산책 채팅방이 아님 |
| 403 | CH005 | 1:1 채팅방이 아님 |
| 409 | CH006 | 이미 산책 확정함 |
| 409 | CH007 | 작성자가 동일 스레드의 다른 채팅방을 이미 확정함 |

---

### 산책 참가 확정 상태 조회 (1:1 채팅 전용)

**Endpoint**: `GET /chat-rooms/{chatRoomId}/walk-confirm`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "chatRoomId": 500,
    "myConfirmedAt": "2026-01-27T10:00:00+09:00",
    "otherConfirmedAt": "2026-01-27T10:05:00+09:00",
    "isWalkConfirmed": true
  }
}
```

---

### 산책 참가 확정 취소 (1:1 채팅 전용)

**Endpoint**: `DELETE /chat-rooms/{chatRoomId}/walk-confirm`

**설명**: 본인이 누른 산책 확정을 취소합니다. **상대가 아직 확정하지 않은 경우에만** 취소할 수 있습니다.

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "chatRoomId": 500,
    "myConfirmedAt": null,
    "otherConfirmedAt": null,
    "isWalkConfirmed": false
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 403 | CH003 | 채팅방 참여자가 아님 |
| 403 | CH004 | 산책 채팅방이 아님 |
| 403 | CH005 | 1:1 채팅방이 아님 |
| 409 | CH008 | 아직 산책 확정하지 않음 |
| 409 | CH009 | 상대가 이미 확정하여 취소할 수 없음 |

---

### 산책 후기 작성 (매너 점수)

**Endpoint**: `POST /chat-rooms/{chatRoomId}/reviews`

**설명**:
- 산책 종료 후, 채팅방 참여자가 다른 참여자에게 1~10점 후기를 작성합니다.
- **그룹 채팅**: 같은 채팅방 참가자 서로 모두 평가 가능
- **1:1 채팅**: 산책 참가 확정이 완료된 채팅방에서만 서로 평가 가능
- 작성된 점수는 대상 회원의 매너 온도(평균)에 반영됩니다.

**Request Body**
```json
{
  "targetMemberId": 2,
  "score": 9,
  "comment": "약속 시간 잘 지키고 배려심이 있어요!"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 900,
    "chatRoomId": 500,
    "reviewerId": 1,
    "targetMemberId": 2,
    "score": 9,
    "comment": "약속 시간 잘 지키고 배려심이 있어요!",
    "createdAt": "2026-01-27T18:10:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | CH010 | 점수 범위 오류 (1~10) |
| 400 | CH011 | 대상 회원이 유효하지 않음 (본인/미참여자) |
| 403 | CH003 | 채팅방 참여자가 아님 |
| 403 | CH004 | 산책 채팅방이 아님 |
| 403 | CH012 | 산책 참가 확정이 필요함 (1:1 채팅) |
| 403 | CH013 | 산책이 아직 종료되지 않음 |
| 409 | CH014 | 이미 후기 작성함 |

---

### 내가 작성한 후기 목록 조회 (채팅방 기준)

**Endpoint**: `GET /chat-rooms/{chatRoomId}/reviews/me`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 900,
      "targetMemberId": 2,
      "score": 9,
      "comment": "약속 시간 잘 지키고 배려심이 있어요!",
      "createdAt": "2026-01-27T18:10:00+09:00"
    }
  ]
}
```

---

## 3.7 실종 반려견 (Lost Pets)

### 실종/제보 이미지 사전 분석 (MVP)

**Endpoint**: `POST /lost-pets/analyze`

**설명**:
- 사진을 사전 분석하여 품종/특징 후보를 반환합니다.
- 본 API는 등록 전 프리뷰 용도이며, 최종 저장은 `POST /lost-pets` 또는 `POST /sightings`에서 수행합니다.

**Request Body**
```json
{
  "image": "data:image/jpeg;base64,..."
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "breed": {
      "ko": "포메라니안",
      "en": "Pomeranian"
    },
    "features": [
      {"ko": "갈색 털", "en": "brown fur"},
      {"ko": "빨간 목줄", "en": "red collar"}
    ],
    "confidence": 0.87,
    "previewImageUrl": "https://cdn.aini-inu.com/lost-pets/previews/uuid.jpg"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | L001 | 사진에서 강아지가 감지되지 않음 |
| 400 | C001 | 지원하지 않는 이미지 형식/요청 본문 오류 |

---

### 실종 등록

**Endpoint**: `POST /lost-pets`

**Request Body**
```json
{
  "petId": 1,
  "photoUrls": ["https://s3.../lost1.jpg", "https://s3.../lost2.jpg"],
  "textFeatures": "갈색 포메라니안, 엉덩이에 검은 점, 빨간 목줄 착용",
  "description": "한강공원에서 산책 중 놓쳤습니다. 겁이 많아서 도망칠 수 있어요.",
  "lastSeenLocation": {
    "placeName": "여의도 한강공원",
    "latitude": 37.5283,
    "longitude": 126.9328,
    "address": "서울특별시 영등포구 여의동로 330"
  },
  "lastSeenAt": "2026-01-26T14:30:00+09:00",
  "isNotificationEnabled": true
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 50,
    "pet": {
      "id": 1,
      "name": "몽이",
      "breed": "포메라니안",
      "photoUrl": "https://s3.../pet.jpg"
    },
    "photoUrls": ["https://s3.../lost1.jpg"],
    "croppedPhotoUrl": "https://s3.../lost1_cropped.jpg",
    "textFeatures": "갈색 포메라니안, 엉덩이에 검은 점, 빨간 목줄 착용",
    "description": "한강공원에서 산책 중 놓쳤습니다.",
    "lastSeenLocation": {
      "placeName": "여의도 한강공원",
      "latitude": 37.5283,
      "longitude": 126.9328
    },
    "lastSeenAt": "2026-01-26T14:30:00+09:00",
    "status": "SEARCHING",
    "isNotificationEnabled": true,
    "similarSightingsCount": 3,
    "createdAt": "2026-01-26T15:00:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | L001 | 사진에서 강아지가 감지되지 않음 |
| 403 | P006 | 본인 소유 반려견이 아님 |
| 409 | L002 | 이미 실종 신고된 반려견 |

---

### 내 실종 신고 목록

**Endpoint**: `GET /lost-pets/mine`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 50,
      "pet": {"id": 1, "name": "몽이", "photoUrl": "https://s3.../pet.jpg"},
      "status": "SEARCHING",
      "lastSeenAt": "2026-01-26T14:30:00+09:00",
      "newSightingsCount": 2,
      "createdAt": "2026-01-26T15:00:00+09:00"
    }
  ]
}
```

---

### 실종 신고 상세

**Endpoint**: `GET /lost-pets/{lostPetId}`

**Response (200 OK)**: 상세 정보 반환

---

### 유사 제보 목록 조회

**Endpoint**: `GET /lost-pets/{lostPetId}/similar-sightings`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| radius | integer | X | 10 | 반경 (km) |
| days | integer | X | 30 | 기간 (일) |
| page | integer | X | 0 | 페이지 (0부터) |
| size | integer | X | 20 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 200,
        "photoUrl": "https://s3.../sighting1.jpg",
        "foundLocation": {
          "placeName": "영등포 타임스퀘어 근처",
          "latitude": 37.5170,
          "longitude": 126.9033
        },
        "foundAt": "2026-01-26T16:00:00+09:00",
        "similarityScore": {
          "total": 0.78,
          "image": 0.85,
          "location": 0.6,
          "time": 0.9
        },
        "distanceKm": 3.2,
        "hoursAgo": 2
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 8,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### 유사 실종 목록 조회 (제보 기준)

**Endpoint**: `GET /sightings/{sightingId}/similar-lost-pets`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| radius | integer | X | 10 | 반경 (km) |
| days | integer | X | 30 | 기간 (일) |
| page | integer | X | 0 | 페이지 (0부터) |
| size | integer | X | 20 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 50,
        "pet": {
          "id": 1,
          "name": "몽이",
          "photoUrl": "https://s3.../pet.jpg"
        },
        "lastSeenLocation": {
          "placeName": "여의도 한강공원",
          "latitude": 37.5283,
          "longitude": 126.9328
        },
        "lastSeenAt": "2026-01-26T14:30:00+09:00",
        "similarityScore": {
          "total": 0.81,
          "image": 0.88,
          "location": 0.67,
          "time": 0.91
        },
        "distanceKm": 2.9,
        "hoursAgo": 3
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 4,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### "이 아이예요!" (매칭 요청)

**Endpoint**: `POST /lost-pets/{lostPetId}/match`

**Request Body**
```json
{
  "sightingId": 200
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "chatRoomId": 600,
    "sighting": {
      "id": 200,
      "finder": {
        "id": 5,
        "nickname": "길가던사람"
      }
    }
  }
}
```

---

### 실종 등록 종료

**Endpoint**: `PATCH /lost-pets/{lostPetId}/close`

**Request Body**
```json
{
  "reason": "FOUND"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 50,
    "status": "FOUND",
    "closedAt": "2026-01-26T18:00:00+09:00"
  }
}
```

---

### 제보 등록

**Endpoint**: `POST /sightings`

**Request Body**
```json
{
  "photoUrl": "https://s3.../found_dog.jpg",
  "description": "공원 벤치 근처에서 배회하고 있었어요",
  "foundLocation": {
    "placeName": "영등포 타임스퀘어 근처",
    "latitude": 37.5170,
    "longitude": 126.9033,
    "address": "서울특별시 영등포구 영중로 15"
  },
  "foundAt": "2026-01-26T16:00:00+09:00"
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 201,
    "photoUrl": "https://s3.../found_dog.jpg",
    "croppedPhotoUrl": "https://s3.../found_dog_cropped.jpg",
    "description": "공원 벤치 근처에서 배회하고 있었어요",
    "foundLocation": {
      "placeName": "영등포 타임스퀘어 근처",
      "latitude": 37.5170,
      "longitude": 126.9033
    },
    "foundAt": "2026-01-26T16:00:00+09:00",
    "potentialMatchCount": 2,
    "createdAt": "2026-01-26T16:30:00+09:00"
  }
}
```

---

### 내 제보 목록

**Endpoint**: `GET /sightings/mine`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| status | string | X | - | 상태 필터 (ACTIVE, MATCHED) |
| page | integer | X | 0 | 페이지 (0부터) |
| size | integer | X | 20 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 201,
        "photoUrl": "https://s3.../found_dog.jpg",
        "foundLocation": {
          "placeName": "영등포 타임스퀘어 근처",
          "latitude": 37.5170,
          "longitude": 126.9033
        },
        "foundAt": "2026-01-26T16:00:00+09:00",
        "status": "ACTIVE",
        "potentialMatchCount": 2,
        "createdAt": "2026-01-26T16:30:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### 제보 상세 (제보자 본인)

**Endpoint**: `GET /sightings/{sightingId}`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 201,
    "photoUrl": "https://s3.../found_dog.jpg",
    "croppedPhotoUrl": "https://s3.../found_dog_cropped.jpg",
    "description": "공원 벤치 근처에서 배회하고 있었어요",
    "foundLocation": {
      "placeName": "영등포 타임스퀘어 근처",
      "latitude": 37.5170,
      "longitude": 126.9033,
      "address": "서울특별시 영등포구 영중로 15"
    },
    "foundAt": "2026-01-26T16:00:00+09:00",
    "status": "ACTIVE",
    "createdAt": "2026-01-26T16:30:00+09:00",
    "updatedAt": "2026-01-26T16:30:00+09:00"
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 403 | L003 | 본인 제보가 아님 |
| 404 | L004 | 제보를 찾을 수 없음 |

---

### 제보 삭제 (제보자 본인)

**Endpoint**: `DELETE /sightings/{sightingId}`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 403 | L003 | 본인 제보가 아님 |
| 404 | L004 | 제보를 찾을 수 없음 |

---

## 3.8 커뮤니티 (Community)

### 게시물 작성

**Endpoint**: `POST /posts`

**Request Body**
```json
{
  "content": "오늘 몽이랑 한강 산책 다녀왔어요! 날씨가 너무 좋았습니다 ☀️",
  "imageUrls": ["https://s3.../post1.jpg", "https://s3.../post2.jpg"]
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": 1000,
    "author": {
      "id": 1,
      "nickname": "몽이아빠",
      "profileImageUrl": "https://s3.../profile.jpg"
    },
    "content": "오늘 몽이랑 한강 산책 다녀왔어요! 날씨가 너무 좋았습니다 ☀️",
    "imageUrls": ["https://s3.../post1.jpg", "https://s3.../post2.jpg"],
    "likeCount": 0,
    "commentCount": 0,
    "createdAt": "2026-01-26T17:00:00+09:00"
  }
}
```

---

### 게시물 목록 조회

**Endpoint**: `GET /posts`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |
| sort | string | X | -createdAt | 정렬 |
| memberId | long | X | - | 특정 작성자 게시물만 조회 |
| location | string | X | - | 지역 문자열 필터 (contains) |

**설명**:
- 무한 스크롤을 위해 `SliceResponse` 형태로 응답합니다.

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1000,
        "author": {
          "id": 1,
          "nickname": "몽이아빠",
          "profileImageUrl": "https://s3.../profile.jpg"
        },
        "content": "오늘 몽이랑 한강 산책 다녀왔어요!",
        "imageUrls": ["https://s3.../post1.jpg"],
        "likeCount": 15,
        "commentCount": 3,
        "isLiked": false,
        "createdAt": "2026-01-26T17:00:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": false,
    "hasNext": true
  }
}
```

---

### 스토리 목록 조회 (Diary 기반)

**Endpoint**: `GET /stories`

**설명**:
- 팔로잉한 회원의 공개 산책 일기(`walk-diary`)를 스토리 카드 형태로 조회합니다.
- 최신순으로 최대 30개를 반환합니다.

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": "wd_1001",
      "memberId": 2,
      "nickname": "뭉치맘",
      "profileImageUrl": "https://s3.../profile.jpg",
      "coverImageUrl": "https://s3.../walk-diary-cover.jpg",
      "walkDate": "2026-01-26",
      "createdAt": "2026-01-26T20:10:00+09:00"
    }
  ]
}
```

---

### 게시물 상세 조회

**Endpoint**: `GET /posts/{postId}`

**Response (200 OK)**: 상세 정보 + 댓글 목록 포함

---

### 게시물 수정

**Endpoint**: `PATCH /posts/{postId}`

---

### 게시물 삭제

**Endpoint**: `DELETE /posts/{postId}`

---

### 좋아요 토글

**Endpoint**: `POST /posts/{postId}/like`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "isLiked": true,
    "likeCount": 16
  }
}
```

---

### 댓글 작성

**Endpoint**: `POST /posts/{postId}/comments`

**Request Body**
```json
{
  "content": "와 너무 귀여워요! 🐕"
}
```

---

### 댓글 목록 조회

**Endpoint**: `GET /posts/{postId}/comments`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |
| sort | string | X | createdAt,asc | 정렬 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 3001,
        "author": {
          "id": 2,
          "nickname": "뭉치맘",
          "profileImageUrl": "https://s3.../profile2.jpg"
        },
        "content": "와 너무 귀여워요! 🐕",
        "createdAt": "2026-01-26T18:10:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": true,
    "hasNext": false
  }
}
```

---

### 댓글 삭제

**Endpoint**: `DELETE /posts/{postId}/comments/{commentId}`

---

## 3.9 차단 (Block)

### 회원 차단

**Endpoint**: `POST /blocks`

**Request Body**
```json
{
  "targetMemberId": 5
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

---

### 차단 목록 조회

**Endpoint**: `GET /blocks`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": [
    {
      "id": 10,
      "blockedMember": {
        "id": 5,
        "nickname": "차단된사람",
        "profileImageUrl": "https://s3.../profile.jpg"
      },
      "createdAt": "2026-01-26T18:00:00+09:00"
    }
  ]
}
```

---

### 차단 해제

**Endpoint**: `DELETE /blocks/{blockId}`

---

## 3.10 알림 (Notifications)

### 알림 목록 조회

**Endpoint**: `GET /notifications`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |
| isRead | boolean | X | - | 읽음 여부 필터 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": 500,
        "type": "CHAT_MESSAGE",
        "title": "새 메시지",
        "content": "몽이아빠님이 메시지를 보냈습니다",
        "targetType": "CHAT_ROOM",
        "targetId": 100,
        "isRead": false,
        "createdAt": "2026-01-26T18:30:00+09:00"
      },
      {
        "id": 501,
        "type": "WALK_APPLICATION",
        "title": "산책 신청",
        "content": "뭉치맘님이 산책을 신청했습니다",
        "targetType": "THREAD",
        "targetId": 100,
        "isRead": false,
        "createdAt": "2026-01-26T18:00:00+09:00"
      },
      {
        "id": 502,
        "type": "LOST_PET_SIMILAR",
        "title": "유사 제보 발견",
        "content": "몽이와 유사한 강아지 제보가 3건 등록되었습니다",
        "targetType": "LOST_PET",
        "targetId": 50,
        "isRead": true,
        "createdAt": "2026-01-26T17:00:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 15,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### 알림 읽음 처리

**Endpoint**: `PATCH /notifications/{notificationId}/read`

**Response (200 OK)**

---

### 전체 알림 읽음 처리

**Endpoint**: `PATCH /notifications/read-all`

**Response (200 OK)**

---

### 알림 설정 조회

**Endpoint**: `GET /notification-settings`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "chatMessage": true,
    "walkApplication": true,
    "lostPetSimilar": true
  }
}
```

---

### 알림 설정 수정

**Endpoint**: `PATCH /notification-settings`

**Request Body**
```json
{
  "chatMessage": true,
  "walkApplication": false,
  "lostPetSimilar": true
}
```

---

## 3.11 이미지 업로드 (Uploads)

> 본 프로젝트의 이미지 관련 API들은 **이미지 URL**만 받습니다.  
> 클라이언트는 먼저 아래 API로 업로드용 Presigned URL을 발급받아 S3에 업로드한 뒤, 반환된 `imageUrl`을 각 도메인 API에 전달합니다.

### Presigned URL 발급

**Endpoint**: `POST /images/presigned-url`

**Request Body**
```json
{
  "purpose": "PET_PHOTO",
  "fileName": "photo.jpg",
  "contentType": "image/jpeg"
}
```

**Request Fields**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| purpose | string | O | 업로드 목적 (PROFILE, PET_PHOTO, POST, WALK_DIARY, LOST_PET, SIGHTING) |
| fileName | string | O | 원본 파일명 |
| contentType | string | O | MIME 타입 (예: image/jpeg) |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "presignedUrl": "https://s3....",
    "imageUrl": "https://cdn.aini-inu.com/....",
    "expiresIn": 300
  }
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|------|------|------|
| presignedUrl | string | S3 업로드용 Presigned URL (PUT 요청에 사용) |
| imageUrl | string | 업로드 완료 후 사용할 최종 이미지 URL |
| expiresIn | integer | URL 유효 기간 (초 단위, 300초=5분) |

**업로드 흐름**

1. 클라이언트가 본 API를 호출하여 `presignedUrl`과 `imageUrl`을 받음
2. 클라이언트가 `presignedUrl`로 직접 S3에 이미지를 PUT 요청 (Content-Type 헤더 포함)
3. 업로드 성공 후, 반환받은 `imageUrl`을 해당 도메인 API(반려견 등록, 게시물 작성 등)에 전달

**S3 경로 구조**

- PROFILE: `profiles/{uuid}.{extension}`
- PET_PHOTO: `pets/{uuid}.{extension}`
- POST: `posts/{uuid}.{extension}`
- WALK_DIARY: `walk-diaries/{uuid}.{extension}`
- LOST_PET: `lost-pets/{uuid}.{extension}`
- SIGHTING: `sightings/{uuid}.{extension}`

**지원 형식**
- `image/jpeg`
- `image/png`
- `image/gif`
- `image/webp`

**URL 유효 기간**: 300초 (5분)

**구현 체크리스트**

- [ ] Purpose별 S3 경로 분리
- [ ] UUID 기반 파일명 생성
- [ ] Content-Type 검증
- [ ] Presigned URL 5분 만료 설정

---

## 3.12 산책 일기 (Walk Diaries)

### 산책 일기 생성

**Endpoint**: `POST /walk-diaries`

**Request Body**
```json
{
  "threadId": 100,
  "title": "서울숲 저녁 산책 기록",
  "content": "오늘은 서울숲에서 산책 메이트와 함께 1시간 산책했어요.",
  "photoUrls": ["https://cdn.aini-inu.com/walk-diaries/uuid1.jpg"],
  "walkDate": "2026-01-26",
  "location": "서울 성동구 서울숲",
  "isPublic": true,
  "tags": [
    {"memberId": 2, "nickname": "뭉치맘"}
  ]
}
```

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "id": "wd_1001",
    "author": {
      "id": 1,
      "nickname": "건홍이네",
      "profileImageUrl": "https://s3.../profile.jpg"
    },
    "threadId": 100,
    "title": "서울숲 저녁 산책 기록",
    "content": "오늘은 서울숲에서 산책 메이트와 함께 1시간 산책했어요.",
    "photoUrls": ["https://cdn.aini-inu.com/walk-diaries/uuid1.jpg"],
    "walkDate": "2026-01-26",
    "location": "서울 성동구 서울숲",
    "isPublic": true,
    "tags": [
      {"memberId": 2, "nickname": "뭉치맘"}
    ],
    "createdAt": "2026-01-26T20:10:00+09:00",
    "updatedAt": "2026-01-26T20:10:00+09:00"
  }
}
```

---

### 산책 일기 목록 조회

**Endpoint**: `GET /walk-diaries`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| memberId | long | X | 내 ID | 특정 회원 일기 조회 |
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |
| sort | string | X | -walkDate | 정렬 |
| visibility | string | X | PUBLIC | 공개 범위 (PUBLIC, ALL) |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": "wd_1001",
        "author": {
          "id": 1,
          "nickname": "건홍이네",
          "profileImageUrl": "https://s3.../profile.jpg"
        },
        "title": "서울숲 저녁 산책 기록",
        "photoUrls": ["https://cdn.aini-inu.com/walk-diaries/uuid1.jpg"],
        "walkDate": "2026-01-26",
        "location": "서울 성동구 서울숲",
        "isPublic": true,
        "createdAt": "2026-01-26T20:10:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": false,
    "hasNext": true
  }
}
```

---

### 산책 일기 상세 조회

**Endpoint**: `GET /walk-diaries/{diaryId}`

**Response (200 OK)**: 산책 일기 전체 필드 반환

---

### 산책 일기 수정

**Endpoint**: `PATCH /walk-diaries/{diaryId}`

**Request Body**: 수정할 필드만 포함

---

### 산책 일기 삭제

**Endpoint**: `DELETE /walk-diaries/{diaryId}`

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": null
}
```

---

### 팔로잉 산책 일기 목록 조회

**Endpoint**: `GET /walk-diaries/following`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | integer | X | 0 | 페이지 번호 (0부터) |
| size | integer | X | 20 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "success": true,
  "status": 200,
  "data": {
    "content": [
      {
        "id": "wd_1002",
        "author": {
          "id": 2,
          "nickname": "뭉치맘",
          "profileImageUrl": "https://s3.../profile2.jpg"
        },
        "title": "저녁 한강 산책",
        "photoUrls": ["https://cdn.aini-inu.com/walk-diaries/uuid2.jpg"],
        "walkDate": "2026-01-25",
        "location": "한강공원",
        "isPublic": true,
        "createdAt": "2026-01-25T19:00:00+09:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "first": true,
    "last": true,
    "hasNext": false
  }
}
```

**Error Codes**

| 상태 코드 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 403 | WD001 | 비공개 일기 접근 불가 |
| 404 | WD002 | 산책 일기를 찾을 수 없음 |
| 403 | WD003 | 산책 일기 작성자만 수정/삭제 가능 |

---

## 3.13 공통 에러 코드

| HTTP 상태 | 에러 코드 | 설명 |
|-----------|-----------|------|
| 400 | C001 | 잘못된 입력값입니다 |
| 400 | C002 | 입력값 검증에 실패했습니다 |
| 401 | C101 | 인증이 필요합니다 |
| 401 | C102 | 유효하지 않은 토큰입니다 |
| 401 | C103 | 만료된 토큰입니다 |
| 401 | C104 | 이메일 또는 비밀번호가 올바르지 않습니다 |
| 403 | C201 | 권한이 없습니다 |
| 404 | C301 | 요청한 리소스를 찾을 수 없습니다 |
| 500 | C999 | 서버 내부 오류가 발생했습니다 |

---
