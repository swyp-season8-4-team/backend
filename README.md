# 🐝 디저비(Desserbee) Backend

> 디저비 백엔드는 **디저트 가게 탐색/검색, 커뮤니티, 사장님 대시보드, 사용자 취향 기반 추천** 기능을 제공하는 REST API 서버입니다.  
> 프론트엔드 모노레포(Next.js)와 연동되어 **가게 데이터, 사용자/사장님 계정, 커뮤니티, 통계** 전반을 담당합니다.

---

## ⚙️ 기술 스택

### Backend

- **Java 17**
- **Spring Boot**
- **Spring Web / Spring MVC**
- **Spring Data JPA**
- (필요 시) Spring Security / OAuth2 Client (소셜 로그인 연동)

### Database

- **MySQL**

### Infra / Tooling

- **AWS EC2** – 애플리케이션 서버
- **Nginx** – 리버스 프록시, 정적 리소스/SSL 처리
- **Swagger** – API 문서 자동화
- (선택) **Docker / Docker Compose**

### 기타

- **GitHub Actions** – CI/CD (테스트, 빌드, 배포 자동화)
- **.env / 환경 분리** – `local`, `dev`, `prod` 등
- Logback 기반 로깅 설정

---

## 📁 폴더 구조

> 실제 프로젝트 구조를 기준으로 요약한 백엔드 디렉토리 구조입니다.

```bash
backend/
├── .github/              # GitHub Actions 워크플로우
├── nginx/                # Nginx 설정 파일 (리버스 프록시, SSL 등)
├── scripts/              # 배포/헬스체크/마이그레이션 스크립트
├── ssl/                  # SSL 인증서 관련 파일 (운영 환경)
├── src/
│   ├── main/
│   │   ├── java/org/swyp/dessertbee/
│   │   │   ├── admin/        # 관리자용 API/도메인
│   │   │   ├── auth/         # 인증/인가, 소셜 로그인, 토큰
│   │   │   ├── common/       # 공통 응답/예외/유틸
│   │   │   ├── community/    # 커뮤니티 (리뷰, 메이트, 댓글 등)
│   │   │   ├── config/       # Spring / Swagger / CORS / JPA 설정
│   │   │   ├── email/        # 이메일 발송, 템플릿 등
│   │   │   ├── migration/    # 마이그레이션/배치 관련 로직
│   │   │   ├── preference/   # 사용자 취향/설문 도메인
│   │   │   ├── role/         # 권한/역할(Role) 도메인
│   │   │   ├── search/       # 검색/필터링 도메인 (가게/키워드)
│   │   │   ├── seeder/       # 초기 데이터 시딩 로직
│   │   │   ├── statistics/   # 통계/분석 (가게/커뮤니티/유저)
│   │   │   ├── store/        # 가게/매장 도메인 (사장님 대시보드 포함)
│   │   │   └── user/         # 사용자/사장님 계정, 프로필
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/java/org/swyp/dessertbee/
│       └── ... 테스트 코드
├── Dockerfile
├── docker-compose.yml
├── build.gradle
└── settings.gradle
```

### 📌 기타

- API 스펙 변경 시, Swagger 문서와 프론트엔드 API 타입 정의를 함께 업데이트합니다.

- 데이터 정합성이 중요한 기능(취향 추천, 검색, 통계 등)은 통합 테스트를 우선적으로 작성합니다.

- 배포 전후에는 health-check 엔드포인트나 scripts/ 디렉터리의 헬스체크 스크립트를 활용해 상태를 점검합니다.

이 README는 디저비 프론트엔드 README와 함께 봤을 때 전체 아키텍처가 한 번에 보이도록 구성했습니다.
필요하면 ERD 다이어그램, 주요 API 시퀀스 다이어그램 섹션도 추가로 정리할 수 있습니다. 🐝
