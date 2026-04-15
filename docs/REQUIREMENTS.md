# 프로젝트 확장 요구사항

## 개요
영쏘(Youngssoo) 앱을 학습 게임 + 미니게임 플랫폼으로 확장

---

## 1. 학습 게임 확장

### 현재 상태
- 영어 (Vocab)
- 수학 (Math)

### 추가 예정
- 한자 (Hanja)

---

## 2. 문제 데이터 관리

### 수학
- 기존 방식 유지 (앱 내에서 곱셈 문제 자동 생성)

### 영어 / 한자
- **외부 저장소**: Firebase Storage 또는 Firestore에 문제 데이터 저장
- **동기화 시점**: 앱 로딩 시 또는 수동 새로고침
- **로컬 캐싱**: Room DB에 저장하여 오프라인 사용 가능
- **데이터 구조 예시**:
  ```json
  {
    "id": "vocab_001",
    "type": "vocab",
    "question": "apple",
    "answer": "사과",
    "options": ["사과", "바나나", "포도", "오렌지"],
    "difficulty": 1
  }
  ```

---

## 3. 포인트 시스템

### 포인트 획득
- 학습 게임 문제를 맞추면 포인트 획득
- 난이도/정답률에 따라 차등 지급 가능

### 포인트 사용
- 미니게임 플레이 구매
  - 시간 기반: N포인트 = M분 플레이
  - 판수 기반: N포인트 = M판 플레이

---

## 4. 미니게임 탭 (2번째 탭)

### UI 구성
- 게임 리스트 화면
- 각 게임별 필요 포인트 표시
- 보유 포인트 표시

### 게임 실행 방식
- **WebView** 사용
- 게임은 HTML/JS로 제작
- 외부 서버에서 호스팅 (Firebase Hosting 등)

### 앱 ↔ 웹 통신

#### 앱 → 웹 (파라미터 전달)
- 유저 ID
- 보유 포인트
- 구매한 플레이 시간/판수
- 기타 설정값

#### 웹 → 앱 (결과 수신)
- 게임 종료 여부
- 획득 점수
- 게임 결과 데이터

#### 기술 구현
| 플랫폼 | 기술 |
|--------|------|
| Android | JavaScript Interface (`@JavascriptInterface`) |
| iOS | WKScriptMessageHandler |

### 게임 업데이트
- HTML/JS 파일은 외부 서버에서 호스팅
- 앱 업데이트 없이 게임 콘텐츠 추가/수정 가능

---

## 5. 기술 스택 정리

| 기능 | 기술 |
|------|------|
| 문제 저장소 | Firebase Firestore / Storage |
| 로컬 DB | Room (기존 활용) |
| 네트워크 | Ktor (기존 활용) |
| 웹뷰 | Android WebView / iOS WKWebView |
| 게임 호스팅 | Firebase Hosting / CDN |
| 앱-웹 통신 | JavaScript Bridge |

---

## 6. 화면 구조 (예상)

```
[탭 1: 학습]          [탭 2: 미니게임]       [탭 3: 보상/마이페이지]
    │                      │                      │
    ├── 수학               ├── 게임 리스트         ├── 포인트 현황
    ├── 영어               └── WebView 게임        ├── 학습 기록
    └── 한자                                       └── 설정
```

---

## 7. 향후 고려사항

- [ ] 문제 데이터 버전 관리 (변경된 문제만 동기화)
- [ ] 오프라인 모드 지원 범위 결정
- [ ] 포인트 악용 방지 (서버 검증)
- [ ] 미니게임 결과 서버 저장 여부
- [ ] 푸시 알림 (새 문제/게임 추가 시)

---

## 8. 구현 완료 (2026-03-25)

### 완료된 기능

1. **Firebase Firestore 연동**
   - `dev.gitlive:firebase-firestore` KMP 라이브러리 추가
   - 문제 데이터 동기화 및 로컬 캐싱

2. **문제 데이터 모델**
   - `QuestionEntity`: Room DB Entity
   - `QuestionDao`: 데이터 접근 객체
   - `QuestionRepository`: Firebase ↔ 로컬 DB 동기화

3. **한자 게임 추가**
   - `HanjaGameViewModel`, `HanjaScreen`
   - Firebase에서 문제 데이터 로드

4. **영어 게임 외부 데이터 연동**
   - `VocabGameViewModel` 리팩토링
   - Firebase 데이터 + 폴백 데이터 지원

5. **미니게임 탭 및 WebView**
   - `MiniGameListScreen`: 게임 리스트 화면
   - `MiniGameViewModel`: 게임 구매/결과 처리
   - `PlatformWebView`: Android/iOS 플랫폼별 WebView 구현
   - JavaScript Bridge를 통한 앱↔웹 통신

6. **탭 네비게이션**
   - 3탭 구조: 학습 / 미니게임 / 마이페이지
   - `App.kt`에서 중앙 관리

---

## 9. Firebase Firestore 데이터 구조

### 컬렉션: `vocab_questions` (영어 문제)
```json
{
  "question": "apple",
  "answer": "사과",
  "options": ["사과", "바나나", "포도", "오렌지", "딸기"],
  "difficulty": 1,
  "updatedAt": 1711350000000
}
```

### 컬렉션: `hanja_questions` (한자 문제)
```json
{
  "question": "山",
  "answer": "산",
  "options": ["산", "강", "바다", "하늘", "땅"],
  "difficulty": 1,
  "updatedAt": 1711350000000
}
```

### 컬렉션: `mini_games` (미니게임)
```json
{
  "name": "퍼즐 게임",
  "description": "조각을 맞춰 그림을 완성하세요",
  "thumbnailUrl": "https://...",
  "gameUrl": "https://your-hosting.com/games/puzzle/index.html",
  "costType": "TIME",
  "costAmount": 100,
  "playValue": 180,
  "screenOrientation": "PORTRAIT"
}
```

`screenOrientation` 값은 `PORTRAIT` 또는 `LANDSCAPE`를 사용합니다.


---

## 10. 미니게임 HTML 작성 가이드

### Android에서 호출 가능한 함수
```javascript
// 점수 업데이트
AndroidBridge.updateScore(100);

// 게임 완료
AndroidBridge.gameComplete(500);

// 로그
AndroidBridge.log("메시지");
```

### iOS에서 호출 가능한 함수
```javascript
// 점수 업데이트
window.webkit.messageHandlers.iOSBridge.postMessage({
  type: 'updateScore',
  score: 100
});

// 게임 완료
window.webkit.messageHandlers.iOSBridge.postMessage({
  type: 'gameComplete',
  score: 500
});
```

### 크로스 플랫폼 헬퍼 예시
```javascript
function sendToApp(type, score) {
  if (window.AndroidBridge) {
    if (type === 'updateScore') AndroidBridge.updateScore(score);
    else if (type === 'gameComplete') AndroidBridge.gameComplete(score);
  } else if (window.webkit && window.webkit.messageHandlers.iOSBridge) {
    window.webkit.messageHandlers.iOSBridge.postMessage({ type, score });
  }
}
```
