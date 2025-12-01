# GSMC Server V3 - Project Context

## Language Requirement
**ALL responses from Claude MUST be in Korean (한국어).**

## Project Overview
GSMC (Gwangju Software Meister High School) Certification Management System - Server V3

This is a student certification and achievement management system for Gwangju Software Meister High School.

### Tech Stack
- **Language**: Kotlin
- **Framework**: Spring Boot 3.x
- **Database**: MySQL (Exposed ORM)
- **Cache**: Redis
- **Build Tool**: Gradle (Kotlin DSL)
- **Authentication**: Google OAuth
- **Code Formatting**: KtLint (following `.editorconfig` rules)

### Key Features
- Student certification and score management
- Project participation tracking
- Evidence submission and approval workflow
- File management system
- OAuth-based authentication

## Architecture & Directory Structure

### Package Structure
```
com.team.incube.gsmc.v3
├── domain/              # Domain-based packages
│   ├── {domain}/
│   │   ├── dto/        # Data Transfer Objects & constants
│   │   ├── entity/     # Exposed/Redis Entities
│   │   ├── repository/ # Repository interfaces & implementations
│   │   ├── service/    # Service interfaces & implementations
│   │   └── presentation/ # Controllers & Request/Response DTOs
│   │       └── data/
│   │           ├── request/
│   │           └── response/
└── global/             # Global configurations & utilities
```

### Domain Modules
- **auth**: Authentication & Authorization
- **member**: User management
- **score**: Score/Achievement management
- **evidence**: Evidence submission
- **project**: Project management
- **file**: File management
- **alert**: Notification system

## Naming Conventions

### Entity Naming
- **RDB Entity**: `{Domain}ExposedEntity`
  - Example: `MemberExposedEntity`, `ScoreExposedEntity`
- **Redis Entity**: `{Domain}RedisEntity`
  - Example: `RefreshTokenRedisEntity`, `EvidenceDraftRedisEntity`

### Repository Naming
- **Repository Interface**: `{Domain}ExposedRepository` or `{Domain}RedisRepository`
- **Implementation**: `{Domain}ExposedRepositoryImpl`
- **Methods**: Follow Spring Data conventions
  - `findById`, `findByMemberId`, `existsById`, etc.

### Service Naming
- **Service Interface**: `{Action}{Domain}Service`
- **Implementation**: `{Action}{Domain}ServiceImpl`

#### Action Keywords
| Action | Usage | Example |
|--------|-------|---------|
| Find | Retrieve data | `FindScoreByIdService` |
| Search | Search with criteria | `SearchProjectsService` |
| Create | Create new entity | `CreateCertificateScoreService` |
| Update | Update existing entity | `UpdateScoreStatusService` |
| Delete | Delete entity | `DeleteScoreService` |
| **My** prefix | Current authenticated user context | `FindMyScoresService`, `UpdateMyCertificateService` |

### DTO Naming
Based on HTTP method and operation:

| Operation | Request DTO | Response DTO |
|-----------|-------------|--------------|
| Read (GET) | - | `Get{Domain}Response` |
| Search (GET) | `Search{Domain}Request` | `Search{Domain}Response` |
| Create (POST) | `Create{Domain}Request` | `Create{Domain}Response` |
| Update (PATCH) | `Patch{Domain}Request` | `Patch{Domain}Response` |
| Delete (DELETE) | - | - |

Example: `GetCertificatesResponse`, `CreateScoreRequest`

### Controller Parameter Naming
```kotlin
@PostMapping("/example")
fun exampleEndPoint(@Valid @RequestBody request: ExampleRequest): ResponseEntity<Void>
```
**Request body parameter name is always `request`**

### Table & URL Naming
- **Table names**: Singular form (`tb_member`, `tb_score`)
- **REST API URLs**: Plural form (`/api/members`, `/api/scores`)

### Code Style
- **Case Convention**:
  - CamelCase: Classes, interfaces, variables, methods
  - snake_case: Database-related (table names, column names)
- **Programming Style**: Functional programming preferred
- **Immutability**: Use `data class` with `val`
- **KtLint**: Strictly applied following `.editorconfig`

## Database Schema

### Core Tables

#### 1. tb_member (Members)
```sql
member_id (PK, AUTO_INCREMENT)
member_name VARCHAR(25)
member_email VARCHAR(50)
member_grade INT NULL
member_class_number INT NULL
member_number INT NULL
member_role ENUM (UNAUTHORIZED, STUDENT, TEACHER, ROOT)
```

#### 2. tb_score (Scores/Achievements)
```sql
score_id (PK, AUTO_INCREMENT)
member_id (FK → tb_member)
category_english_name VARCHAR(100)
score_status ENUM (PENDING, APPROVED, REJECTED, INCOMPLETE)
source_id BIGINT NULL  -- Special column! See below
activity_name VARCHAR(255) NULL
score_value DOUBLE NULL
rejection_reason TEXT NULL

UNIQUE INDEX: (member_id, category_english_name, source_id)
```

**⚠️ IMPORTANT: `source_id` Column**
- **Not a foreign key**, but application-level reference
- References different tables based on `CategoryType.evidenceType`:
  - `EVIDENCE`: Points to `tb_evidence.evidence_id`
  - `FILE`: Points to `tb_file.file_id`
  - `UNREQUIRED`: Can be NULL
- Used to prevent duplicate submissions with UNIQUE constraint

#### 3. tb_evidence (Evidence)
```sql
evidence_id (PK, AUTO_INCREMENT)
member_id (FK → tb_member)
evidence_title VARCHAR(255)
evidence_content TEXT
evidence_created_at TIMESTAMP (default: CURRENT_TIMESTAMP)
evidence_updated_at TIMESTAMP (default: CURRENT_TIMESTAMP)
```

#### 4. tb_file (Files)
```sql
file_id (PK, AUTO_INCREMENT)
member_id (FK → tb_member)
file_original_name VARCHAR(255)
file_stored_name VARCHAR(255)
file_uri VARCHAR(512)
```

**File Relationship Management**:
- Files can be associated with: `project`, `evidence`, `score`
- Junction tables:
  - `tb_project_file` (project_id, file_id)
  - `tb_evidence_file` (evidence_id, file_id)
- **Many-to-Many**: Multiple files per project/evidence

#### 5. tb_project (Projects)
```sql
project_id (PK, AUTO_INCREMENT)
owner_id (FK → tb_member)
project_title VARCHAR(255)
project_description TEXT
```

#### 6. Junction Tables
- `tb_project_participant` (project_id, member_id)
- `tb_project_score` (project_id, score_id, CASCADE DELETE)
- `tb_project_file` (project_id, file_id)
- `tb_evidence_file` (evidence_id, file_id)

#### 7. tb_alert (Notifications)
```sql
alert_id (PK, AUTO_INCREMENT)
alert_sender_id (FK → tb_member)
alert_receiver_id (FK → tb_member)
score_id (FK → tb_score)
alert_type ENUM
alert_is_read BOOLEAN (default: false)
alert_content VARCHAR(255)
alert_created_at TIMESTAMP
```

### Redis Keys
- `refresh_token:{token}` - OAuth refresh tokens (TTL)
- `evidenceDraft:{memberId}` - Evidence drafts
- `projectDraft:{memberId}` - Project drafts

## Key Design Patterns

### Service Layer Architecture

#### Base Services for Score Management
```kotlin
// For count-based scores (certificates, awards, etc.)
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
    ): CreateScoreResponse
}

// For value-based scores (TOPCIT, TOEIC, etc.)
abstract class BaseCreateOrUpdateBasedScoreService(
    protected val scoreExposedRepository: ScoreExposedRepository,
    protected val currentMemberProvider: CurrentMemberProvider,
) {
    protected fun createOrUpdateScore(
        categoryType: CategoryType,
        scoreValue: Double?,
        sourceId: Long?,
        activityName: String? = null,
    ): CreateScoreResponse
}
```

### Repository Pattern (Exposed ORM)
```kotlin
interface ScoreExposedRepository {
    fun save(score: Score): Score
    fun findById(scoreId: Long): Score?
    fun update(score: Score): Score
    fun updateSourceId(scoreId: Long, sourceId: Long)
    fun existsByMemberIdAndCategoryTypeAndSourceId(
        memberId: Long,
        categoryType: CategoryType,
        sourceId: Long
    ): Boolean
}
```

### Transaction Management
- **Service Layer**: Opens transactions using `transaction { }`
- **Repository Layer**: Never opens transactions
- All Exposed DB operations must be inside `transaction { }` block

## Business Logic Deep Dive

### Score Status Workflow
```
1. INCOMPLETE → (Evidence submitted) → PENDING
2. PENDING → (Admin approval) → APPROVED
3. PENDING → (Admin rejection) → REJECTED
```

**Status Details**:
- **PENDING**: Default state, waiting for approval
- **APPROVED**: Approved by admin
- **REJECTED**: Rejected with reason
- **INCOMPLETE**: Special state for project participation scores (no evidence yet)

### Source ID Management

#### For Certificate/File-based Scores
```kotlin
// Certificate creation - file uploaded first
createCertificateScore(
    value = "정보처리기능사",
    fileId = 123  // sourceId = fileId
)
```

#### For Project Participation
```kotlin
// 1. Create score without source (INCOMPLETE)
createProjectParticipationScore(projectId = 1)
// sourceId = null, status = INCOMPLETE

// 2. Submit evidence later
createEvidence(
    scoreId = score.id,
    title = "프로젝트 활동 증빙",
    content = "...",
    fileIds = [1, 2, 3]
)
// sourceId = evidence.id, status → PENDING
```

### Evidence Creation Flow
```kotlin
1. Verify score exists
2. Check if score already has source: existsWithSource(scoreId)
3. Create evidence with files
4. Update score.sourceId = evidence.id
5. If score.status == INCOMPLETE → change to PENDING
```

### UNIQUE Constraint Validation
**Constraint**: `(member_id, category_english_name, source_id)`

**Prevents**:
- Same member submitting same certificate twice
- Same member submitting same evidence for multiple scores
- Duplicate score entries

**Example**:
```kotlin
// Valid: Different sourceIds
(member=1, category=CERTIFICATE, sourceId=1) ✓
(member=1, category=CERTIFICATE, sourceId=2) ✓

// Invalid: Same sourceId
(member=1, category=CERTIFICATE, sourceId=1) ✓
(member=1, category=CERTIFICATE, sourceId=1) ✗ UNIQUE violation
```

## Configuration & Environment

### Profile Structure
- `application.yaml`: Common properties for all environments
- `application-dev.yaml`: Development-specific properties
- `application-prod.yaml`: Production-specific properties

### Environment Variables
- All environment variable examples defined in `.env.example`
- Actual values in `.env` (gitignored)
- Categories:
  - Database credentials
  - Google OAuth credentials
  - Redis configuration
  - JWT secrets
  - File storage paths

### Mock Data
- Managed in `src/main/resources/data.sql`
- Loaded automatically in dev environment
- Gitignored to prevent conflicts
- Use Exposed DDL auto-generation, then insert data

## Development Guidelines

### Exposed ORM Best Practices

#### 1. Transaction Blocks
```kotlin
// ✓ Correct
fun createScore(...) = transaction {
    val score = scoreExposedRepository.save(...)
    // All DB operations here
}

// ✗ Wrong
fun createScore(...) {
    val score = scoreExposedRepository.save(...)  // Error!
}
```

#### 2. Auto-Increment Columns
```sql
-- ✓ Correct: Omit auto-increment column
INSERT INTO tb_member (member_name, member_email, member_role) VALUES (...)

-- ✗ Wrong: Including auto-increment column
INSERT INTO tb_member (member_id, member_name, member_email) VALUES (1, ...)
```

#### 3. Default Values
```kotlin
// Entity definition
object EvidenceExposedEntity : Table("tb_evidence") {
    val createdAt = timestamp("evidence_created_at").default(Instant.now())
}

// ✓ Correct: Omit columns with defaults
INSERT INTO tb_evidence (member_id, evidence_title) VALUES (...)

// Also valid: Specify explicitly
INSERT INTO tb_evidence (..., evidence_created_at) VALUES (..., CURRENT_TIMESTAMP)
```

### Testing Guidelines

#### Unit Tests Required
- All service layer methods must have unit tests
- Use `@Transactional` with rollback for DB tests
- Mock external dependencies (OAuth, file storage)

#### Test Structure
```kotlin
@SpringBootTest
@Transactional
class CreateCertificateScoreServiceTest {
    @Test
    fun `should create certificate score with file`() {
        // given
        val fileId = 1L
        val activityName = "정보처리기능사"

        // when
        val result = service.execute(activityName, fileId)

        // then
        assertThat(result.scoreId).isNotNull()
    }
}
```

### Swagger Documentation
- **Must** document all REST endpoints
- Include:
  - Operation summary and description (Korean)
  - Request/Response examples
  - Possible error codes
  - Authentication requirements

```kotlin
@Operation(
    summary = "자격증 점수 등록",
    description = "자격증 정보와 증빙 파일을 업로드하여 점수를 등록합니다."
)
@ApiResponses(
    ApiResponse(responseCode = "200", description = "등록 성공"),
    ApiResponse(responseCode = "400", description = "잘못된 요청"),
    ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
)
```

## Git Workflow & Conventions

### Commit Message Format
```
<type>: <subject>

<body>
```

#### Commit Types
| Type | Usage |
|------|-------|
| add | Adding new code or files |
| update | Modifying existing code |
| fix | Bug fixes |
| delete | Deleting files or code |
| docs | Documentation updates |
| test | Adding/modifying tests |
| merge | Branch merging |
| init | Project initialization |

### Branch Strategy (Git Flow)
- `main`: Production-ready code
- `develop`: Development integration branch
- `feat/{feature-name}`: New features
- `fix/{issue}`: Bug fixes
- `refactor/{description}`: Code refactoring
- `hotfix/{issue}`: Production hotfixes (merge to main)

### Pull Request Guidelines
- **Approval**: Minimum 1 approval from 5 server developers
- **File Changes**: Keep under ~20 files per PR
- **Frequency**: Make PRs frequently, keep them small
- **Review**: Even if no comments, acknowledge you've read it
- **Templates**: Use provided PR templates
- **Courtesy**: Always be polite and thankful in reviews

## Common Issues & Solutions

### 1. UNIQUE Constraint Violation
**Problem**: Duplicate `(member_id, category, source_id)`

**Solution**:
```kotlin
// Check before creating
if (scoreExposedRepository.existsByMemberIdAndCategoryTypeAndSourceId(
    memberId, categoryType, sourceId
)) {
    throw GsmcException(ErrorCode.SCORE_ALREADY_EXISTS)
}
```

### 2. Source ID Not Set
**Problem**: Creating score without proper `source_id`

**Solution**:
- Certificate/File scores: Pass `fileId` immediately
- Project scores: Create with `sourceId = null`, update later with evidence
- Check `CategoryType.evidenceType` to determine behavior

### 3. Transaction Not Found
**Problem**: Exposed query outside transaction block

**Solution**:
```kotlin
// Wrap all repository calls in transaction
fun execute(...) = transaction {
    repository.save(...)
    repository.findById(...)
}
```

### 4. Mock Data Inconsistency
**Problem**: `data.sql` doesn't match entity definitions

**Solution**:
- Verify column names match entity definitions
- Check data types and constraints
- Test with clean database: drop + recreate + run data.sql

## File & Directory Reference

### Important Files
- `.editorconfig`: Code formatting rules
- `.env.example`: Environment variable template
- `data.sql`: Mock data (gitignored)
- `build.gradle.kts`: Dependencies and build configuration

### Key Directories
- `src/main/kotlin/`: Source code
- `src/main/resources/`: Configuration files
- `src/test/kotlin/`: Test code
- `.claude/`: Claude Code project configuration

## Quick Reference

### Creating New Feature Checklist
1. ✓ Define DTO (if needed)
2. ✓ Create/Update Entity (for new tables)
3. ✓ Create Repository interface + implementation
4. ✓ Create Service interface + implementation
5. ✓ Create Controller + Request/Response DTOs
6. ✓ Write unit tests
7. ✓ Apply KtLint formatting
8. ✓ Add Swagger documentation
9. ✓ Update `data.sql` mock data (if needed)
10. ✓ Test with actual data

### Score Creation Pattern
```kotlin
// 1. Validate inputs
// 2. Check duplicates
// 3. Create score with proper sourceId
// 4. Return response
```

### Evidence Submission Pattern
```kotlin
// 1. Find score by ID
// 2. Validate score doesn't have source yet
// 3. Validate files exist
// 4. Create evidence
// 5. Update score.sourceId
// 6. Change status INCOMPLETE → PENDING (if needed)
```

---

**Remember**: All responses MUST be in Korean (한국어)! 🇰🇷
