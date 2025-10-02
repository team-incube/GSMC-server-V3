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

    // File
    FILE_NOT_FOUND("존재하지 않는 파일입니다.", 404),
}
