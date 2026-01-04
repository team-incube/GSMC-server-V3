package com.team.incube.gsmc.v3.domain.archive.presentation.data.response

/**
 * 아카이브 실행 결과 응답
 */
data class ArchiveScoresResponse(
    val academicYear: Int,
    val archivedCount: Long,
    val message: String,
)
