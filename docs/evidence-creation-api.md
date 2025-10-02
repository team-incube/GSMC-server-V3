# 증빙자료 생성 API 구조 문서

## 개요

증빙자료 생성 API는 여러 점수 객체(Score)를 하나의 증빙자료(Evidence)에 연결하여 학생들의 활동을 증명하는 기능을 제공합니다. 이 문서는 API의 전체적인 작동 구조와 데이터 흐름을 설명합니다.

## API 스펙

### 엔드포인트
- **Method**: POST
- **URL**: `/api/v3/evidences`
- **인증**: Bearer Token (JWT)

### 요청 형식

```json
{
  "scoreIds": [1, 2, 3],
  "title": "증빙자료 제목",
  "content": "증빙자료 내용",
  "fileId": [1, 2]
}
```

### 응답 형식

```json
{
  "status": "CREATED",
  "code": 201,
  "message": "증빙자료가 성공적으로 생성되었습니다.",
  "data": {
    "id": 1,
    "title": "증빙자료 제목",
    "content": "증빙자료 내용",
    "createAt": "2025-10-02T10:30:00",
    "updateAt": "2025-10-02T10:30:00",
    "file": [
      {
        "fileOriginalName": "certificate.pdf",
        "fileStoredName": "stored_name.pdf",
        "fileUri": "https://example.com/files/stored_name.pdf"
      }
    ]
  }
}
```

## 데이터베이스 구조

### 테이블 관계도

```
tb_member (회원)
    ↓ (1:N)
tb_score (점수)  ←─── evidence_id ─────┐
    ↓ (N:1)                          │
tb_category (카테고리)                │
                                     │
tb_evidence (증빙자료) ──────────────┘
    ↓ (1:N)
tb_evidence_file (증빙-파일 연결)
    ↓ (N:1)
tb_file (파일)
```

### 핵심 테이블 구조

#### tb_evidence (증빙자료)
- `evidence_id`: 증빙자료 고유 ID (Primary Key)
- `evidence_title`: 증빙자료 제목
- `evidence_content`: 증빙자료 내용
- `evidence_created_at`: 생성일시
- `evidence_updated_at`: 수정일시

#### tb_score (점수)
- `score_id`: 점수 고유 ID (Primary Key)
- `member_id`: 회원 ID (Foreign Key)
- `category_id`: 카테고리 ID (Foreign Key)
- `score_status`: 점수 상태 (PENDING, APPROVED 등)
- `evidence_id`: 증빙자료 ID (Foreign Key, nullable)

#### tb_evidence_file (증빙-파일 연결)
- `evidence_id`: 증빙자료 ID (Foreign Key)
- `file_id`: 파일 ID (Foreign Key)

## 아키텍처 구조

### 레이어 구조

```
Presentation Layer (Controller)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Entity Layer (Database)
```

### 컴포넌트 다이어그램

```
EvidenceController
    ↓
CreateEvidenceService
    ├── ScoreExposedRepository
    ├── FileExposedRepository
    └── EvidenceExposedRepository
```

## 처리 흐름

### 1. 요청 검증 단계

#### 1.1 입력 데이터 검증
- `scoreIds`: 빈 배열이 아닌지 확인
- `title`: 빈 문자열이 아닌지 확인
- `content`: 빈 문자열이 아닌지 확인

#### 1.2 비즈니스 규칙 검증
- 모든 `scoreIds`가 데이터베이스에 존재하는지 확인
- 제공된 `scoreIds` 중 이미 다른 증빙자료와 연결된 점수가 있는지 확인
- `fileId`가 제공된 경우, 모든 파일이 존재하는지 확인

### 2. 데이터 생성 단계

#### 2.1 증빙자료 생성
1. `tb_evidence` 테이블에 새 레코드 삽입
2. 자동 생성된 `evidence_id` 반환

#### 2.2 파일 연결
1. `fileId`가 제공된 경우
2. `tb_evidence_file` 테이블에 연결 레코드들 일괄 삽입

#### 2.3 점수 업데이트
1. 제공된 `scoreIds`에 해당하는 모든 점수 레코드의 `evidence_id` 컬럼 업데이트
2. 여러 점수가 하나의 증빙자료를 참조하도록 설정

### 3. 응답 생성 단계

#### 3.1 데이터 조회
- 생성된 증빙자료 정보 조회
- 연결된 파일 정보 조회

#### 3.2 응답 변환
- Domain 객체를 Response DTO로 변환
- 공통 API 응답 형식으로 래핑

## 에러 처리

### HTTP 상태 코드

| 상태 코드 | 설명 | 발생 조건 |
|----------|------|----------|
| 201 | Created | 증빙자료 생성 성공 |
| 404 | Not Found | 존재하지 않는 점수 객체 또는 파일 |
| 409 | Conflict | 이미 증빙을 가진 점수가 포함됨 |
| 500 | Internal Server Error | 서버 내부 오류 |

### 에러 응답 예시

```json
{
  "status": "NOT_FOUND",
  "code": 404,
  "message": "존재하지 않는 점수 객체입니다."
}
```

## 트랜잭션 관리

### 트랜잭션 범위
- 전체 증빙자료 생성 과정이 하나의 트랜잭션으로 처리
- 어느 단계에서든 실패 시 전체 롤백

### 트랜잭션 순서
1. 증빙자료 생성
2. 파일 연결 (있는 경우)
3. 점수 업데이트
4. 커밋

## 성능 최적화

### 배치 처리
- 파일 연결: `batchInsert` 사용
- 점수 업데이트: 단일 `update` 쿼리로 여러 레코드 동시 처리

### 쿼리 최적화
- 존재 여부 확인: `COUNT` 쿼리 사용
- 파일 정보 조회: `IN` 절을 사용한 일괄 조회

## 확장 가능성

### 향후 확장 포인트
- 증빙자료 승인 워크플로우 추가
- 파일 업로드 API와의 연동
- 증빙자료 수정/삭제 기능
- 알림 시스템 연동

### 설계 원칙
- 단일 책임 원칙: 각 Service는 하나의 기능만 담당
- 의존성 역전 원칙: Interface를 통한 느슨한 결합
- 개방-폐쇄 원칙: 기능 확장에 열려있고 수정에 닫힌 구조
