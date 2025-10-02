package com.team.incube.gsmc.v3.global.common.error

enum class ErrorCode(
    val message: String,
    val status: Int,
) {
    // Evidence
    EVIDENCE_NOT_FOUND("해당 증빙자료를 찾을 수 없습니다.", 404),
}
