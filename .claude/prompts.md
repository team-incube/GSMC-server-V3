# GSMC Server V3 - Project Prompts

## Project Overview
GSMC(Gwangju Software Meister High School) 학생 관리 시스템 서버 V3

### Tech Stack
- **Language**: Kotlin
- **Framework**: Spring Boot 3.x
- **Database**: MySQL (Exposed ORM)
- **Cache**: Redis
- **Build Tool**: Gradle (Kotlin DSL)

## Architecture & Conventions

### Package Structure
```
com.team.incube.gsmc.v3
├── domain/              # 도메인별 패키지
│   ├── {domain}/
│   │   ├── dto/        # Data Transfer Objects
│   │   ├── entity/     # Exposed Entities
│   │   ├── repository/ # Repository 인터페이스 및 구현체
│   │   ├── service/    # Service 인터페이스 및 구현체
│   │   └── presentation/ # Controller 및 요청/응답 DTO
└── global/             # 전역 설정 및 유틸리티
```

### Naming Conventions
- **Service**: `{Action}{Domain}Service` (예: CreateCertificateScoreService)
- **Service Impl**: `{Action}{Domain}ServiceImpl`
- **Repository**: `{Domain}ExposedRepository`
- **Entity**: `{Domain}ExposedEntity` (Exposed ORM)
- **Redis Entity**: `{Domain}RedisEntity`

### Code Style
- KtLint 포맷팅 적용
- 함수형 프로그래밍 스타일 선호
- 불변 객체 사용 (data class with val)

## Database Schema

### Core Tables
1. **tb_member**: 회원 정보
2. **tb_file**: 파일 정보
3. **tb_score**: 점수/성적 정보
   - UNIQUE INDEX: (member_id, category_english_name, source_id)
   - source_id: 증빙 파일(file_id) 또는 증빙자료(evidence_id) 참조
4. **tb_evidence**: 증빙자료
5. **tb_project**: 프로젝트 정보
6. **tb_project_participant**: 프로젝트 참여자 (중간 테이블)
7. **tb_project_score**: 프로젝트-점수 연결 (중간 테이블)
8. **tb_alert**: 알림

### Redis Keys
- `refresh_token`: 리프레시 토큰 (TTL)
- `evidenceDraft`: 증거자료 임시저장
- `projectDraft`: 프로젝트 임시저장

## Key Design Patterns

### Service Layer Pattern
```kotlin
// Base Service for Count-based scores
abstract class BaseCountBasedScoreService(
    protected val scoreExposedRepository: ScoreExposedRepository,
    protected val currentMemberProvider: CurrentMemberProvider,
) {
    protected fun createScore(
        member: Member,
        categoryType: CategoryType,
        activityName: String,
        sourceId: Long?,
        status: ScoreStatus = ScoreStatus.PENDING,
    ): CreateScoreResponse { ... }
}
```

### Repository Pattern (Exposed ORM)
```kotlin
interface ScoreExposedRepository {
    fun save(score: Score): Score
    fun findById(scoreId: Long): Score?
    fun update(score: Score): Score
    // ... other methods
}
```

## Important Business Logic

### Score Source ID
- `source_id`는 증빙 자료를 가리킴
- 자격증, 시험 점수 등: `file_id` 참조
- 프로젝트 참여 점수: 처음엔 NULL, 이후 Evidence 추가 시 `evidence_id` 업데이트
- UNIQUE 제약으로 중복 등록 방지: `(member_id, category, source_id)`

### Score Status Flow
1. **PENDING**: 대기 (기본 상태)
2. **APPROVED**: 승인됨
3. **REJECTED**: 반려됨
4. **INCOMPLETE**: 미완성 (프로젝트 참여 점수 생성 시)

### Evidence Creation Flow
1. Score 존재 확인
2. 이미 source_id가 있는지 확인 (`existsWithSource`)
3. Evidence 생성
4. Score의 source_id를 evidence_id로 업데이트
5. Status가 INCOMPLETE면 PENDING으로 변경

## Testing Guidelines
- Service 단위 테스트 필수
- 트랜잭션 롤백 전략 사용
- Mock 데이터는 `data.sql` 참조

## Development Tips
1. **Exposed ORM 사용 시 주의사항**:
   - `transaction { }` 블록 내에서 DB 작업 수행
   - Auto increment 컬럼은 INSERT 시 생략
   - Timestamp 컬럼은 `default(Instant.now())` 활용

2. **source_id 설정**:
   - 파일 기반 증빙: 생성 시 `fileId` 전달
   - Evidence 기반: 생성 후 `updateSourceId` 호출

3. **UNIQUE 제약 위반 방지**:
   - 같은 카테고리에 동일한 source로 중복 등록 불가
   - `existsByMemberIdAndCategoryTypeAndSourceId` 사용하여 검증

## Common Issues & Solutions

### 1. UNIQUE 제약 위반
**문제**: 같은 회원이 같은 카테고리에 동일 source로 중복 등록
**해결**:
- source_id를 NULL이 아닌 실제 file_id 또는 evidence_id로 설정
- 중복 체크 로직 추가

### 2. Exposed DDL vs Mock Data 불일치
**문제**: data.sql의 컬럼명이나 타입이 Entity와 다름
**해결**:
- Entity 정의 확인
- AUTO_INCREMENT 컬럼은 INSERT 시 생략
- Default 값이 있는 컬럼은 생략 가능

### 3. Transaction 관리
**문제**: 트랜잭션 외부에서 Exposed 쿼리 실행
**해결**: 모든 DB 작업을 `transaction { }` 블록 내에서 수행

## File Structure Example
```
domain/score/
├── dto/
│   ├── Score.kt
│   └── constant/
│       └── ScoreStatus.kt
├── entity/
│   └── ScoreExposedEntity.kt
├── repository/
│   ├── ScoreExposedRepository.kt
│   └── impl/
│       └── ScoreExposedRepositoryImpl.kt
├── service/
│   ├── CreateCertificateScoreService.kt
│   ├── BaseCountBasedScoreService.kt
│   └── impl/
│       └── CreateCertificateScoreServiceImpl.kt
└── presentation/
    └── data/
        ├── request/
        └── response/
```

## When Adding New Features
1. DTO 정의 (필요시)
2. Entity 정의 (새 테이블인 경우)
3. Repository 인터페이스 및 구현
4. Service 인터페이스 및 구현
5. Controller 및 Request/Response DTO
6. 단위 테스트 작성
7. KtLint 포맷팅 적용