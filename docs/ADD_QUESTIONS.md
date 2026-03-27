# 영어 단어 / 한자 추가 방법

현재 두 가지 방법이 있습니다.

---

## 방법 1: 코드에서 직접 추가 (Fallback 데이터)

Firebase 연동 전까지 사용되는 기본 데이터입니다.

### 영어 단어

파일: `composeApp/src/commonMain/kotlin/com/bium/youngssoo/game/vocab/presentation/VocabGameViewModel.kt`

```kotlin
private val fallbackWords = listOf(
    VocabWord("1", "apple", "사과"),
    VocabWord("2", "banana", "바나나"),
    VocabWord("3", "car", "자동차"),
    VocabWord("4", "bridge", "다리"),
    VocabWord("5", "effort", "노력"),
    VocabWord("6", "success", "성공"),
    VocabWord("7", "failure", "실패"),
    VocabWord("8", "passion", "열정"),
    VocabWord("9", "courage", "용기"),
    VocabWord("10", "dream", "꿈"),
    // 여기에 추가
    VocabWord("11", "school", "학교"),
    VocabWord("12", "friend", "친구"),
)
```

### 한자

파일: `composeApp/src/commonMain/kotlin/com/bium/youngssoo/game/hanja/presentation/HanjaGameViewModel.kt`

```kotlin
private val fallbackWords = listOf(
    HanjaWord("1", "山", "산"),
    HanjaWord("2", "水", "물"),
    HanjaWord("3", "火", "불"),
    HanjaWord("4", "木", "나무"),
    HanjaWord("5", "金", "쇠/금"),
    HanjaWord("6", "土", "흙"),
    HanjaWord("7", "日", "해/날"),
    HanjaWord("8", "月", "달"),
    HanjaWord("9", "人", "사람"),
    HanjaWord("10", "天", "하늘"),
    // 여기에 추가
    HanjaWord("11", "學", "배울 학"),
    HanjaWord("12", "校", "학교 교"),
)
```

### 주의사항
- ID는 고유해야 합니다
- 최소 5개 이상의 단어가 있어야 5지선다가 정상 동작합니다
- 코드 수정 후 앱을 다시 빌드해야 합니다

---

## 방법 2: Firebase Firestore (권장)

앱 업데이트 없이 문제를 추가/수정할 수 있습니다.

### 1. Firebase Console 설정

1. [Firebase Console](https://console.firebase.google.com) 접속
2. 프로젝트: `youngssoo-app` 선택
3. 좌측 메뉴 → Firestore Database
4. 컬렉션 생성

### 2. 컬렉션 구조

#### `vocab_questions` (영어 단어)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| question | string | 영어 단어 | "apple" |
| answer | string | 한글 뜻 | "사과" |
| options | array | 5지선다 보기 | ["사과", "바나나", "포도", "오렌지", "딸기"] |
| difficulty | number | 난이도 (1~3) | 1 |
| updatedAt | number | 타임스탬프 | 1234567890 |

```json
{
  "question": "apple",
  "answer": "사과",
  "options": ["사과", "바나나", "포도", "오렌지", "딸기"],
  "difficulty": 1,
  "updatedAt": 1234567890
}
```

#### `hanja_questions` (한자)

| 필드 | 타입 | 설명 | 예시 |
|------|------|------|------|
| question | string | 한자 | "山" |
| answer | string | 뜻 | "산" |
| options | array | 5지선다 보기 | ["산", "물", "불", "나무", "흙"] |
| difficulty | number | 난이도 (1~3) | 1 |
| updatedAt | number | 타임스탬프 | 1234567890 |

```json
{
  "question": "山",
  "answer": "산",
  "options": ["산", "물", "불", "나무", "흙"],
  "difficulty": 1,
  "updatedAt": 1234567890
}
```

### 3. Firebase 연동 활성화

현재 `HttpClient`가 null로 설정되어 있어서 Firebase가 비활성화 상태입니다.

파일: `composeApp/src/commonMain/kotlin/com/bium/youngssoo/di/Modules.kt`

```kotlin
// 현재 (비활성화)
single { QuestionRepository(get(), null) }

// 활성화하려면 아래로 변경
single { QuestionRepository(get(), get<HttpClient>()) }
```

### 4. 문제 동기화 동작

1. 앱 시작 시 Firebase에서 문제 데이터 동기화
2. 로컬 Room DB에 캐시 저장
3. 오프라인에서도 캐시된 문제로 플레이 가능
4. 강제 새로고침: `refreshQuestions()` 호출

---

## 추천 방식

| 상황 | 추천 방법 |
|------|-----------|
| 테스트/개발 중 | 방법 1 (코드에 직접 추가) |
| 운영/배포 후 | 방법 2 (Firebase) |

Firebase를 사용하면:
- 앱 업데이트 없이 문제 추가/수정/삭제 가능
- 난이도별 문제 관리 가능
- 실시간 업데이트 가능

---

## 관련 파일

| 파일 | 설명 |
|------|------|
| `VocabGameViewModel.kt` | 영어 단어 게임 로직 + fallback 데이터 |
| `HanjaGameViewModel.kt` | 한자 게임 로직 + fallback 데이터 |
| `QuestionRepository.kt` | Firebase 연동 + 로컬 DB 캐시 |
| `QuestionEntity.kt` | Room DB 엔티티 |
| `QuestionDao.kt` | Room DB DAO |
