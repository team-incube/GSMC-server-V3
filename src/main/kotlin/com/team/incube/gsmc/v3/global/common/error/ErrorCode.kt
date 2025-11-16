package com.team.incube.gsmc.v3.global.common.error

enum class ErrorCode(
    val message: String,
    val status: Int,
) {
    // Evidence
    EVIDENCE_NOT_FOUND("해당 증빙자료를 찾을 수 없습니다.", 404),

    // Score
    SCORE_NOT_FOUND("존재하지 않는 점수 객체입니다.", 404),
    SCORE_ALREADY_HAS_EVIDENCE("이미 증빙을 가진 점수가 포함되어 있습니다.", 409),
    SCORE_MAX_LIMIT_EXCEEDED("해당 인증제 항목의 점수 한도를 초과했습니다.", 409),

    // File
    FILE_NOT_FOUND("존재하지 않는 파일입니다.", 404),
    FILE_EMPTY("파일이 비어있습니다.", 400),
    FILE_EXTENSION_NOT_FOUND("파일 확장자가 없습니다.", 400),
    FILE_EXTENSION_NOT_ALLOWED("허용되지 않는 파일 형식입니다.", 400),

    // Category
    INVALID_CATEGORY("유효하지 않은 카테고리입니다.", 500),

    // S3
    S3_FILE_UPLOAD_FAILED("파일 업로드 중 오류가 발생했습니다.", 500),
    S3_FILE_DELETE_FAILED("파일 삭제 중 오류가 발생했습니다.", 500),
    S3_IO_ERROR("파일 입출력 중 오류가 발생했습니다.", 500),
    S3_SERVICE_ERROR("AWS 서비스에서 오류가 발생했습니다.", 500),
    S3_CLIENT_ERROR("AWS SDK 클라이언트에서 오류가 발생했습니다.", 500),

    // Auth
    REFRESH_TOKEN_INVALID("리프레시 토큰이 만료되었거나 유효하지 않습니다.", 401),
    OAUTH2_AUTHORIZATION_FAILED("OAuth 2.0 인증에 실패했습니다.", 401),
    AUTHENTICATION_FAILED("인증 과정에서 오류가 발생했습니다.", 401),

    // Member
    MEMBER_NOT_FOUND("존재하지 않는 사용자입니다.", 404),

    // Project
    PROJECT_NOT_FOUND("존재하지 않는 프로젝트입니다.", 404),
    PROJECT_FORBIDDEN("프로젝트에 접근 권한이 없습니다.", 403),
}
