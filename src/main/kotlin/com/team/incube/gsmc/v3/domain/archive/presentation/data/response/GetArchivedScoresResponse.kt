package com.team.incube.gsmc.v3.domain.archive.presentation.data.response

import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchive

/**
 * 아카이브 조회 응답
 */
data class GetArchivedScoresResponse(
    val academicYear: Int,
    val totalCount: Int,
    val archives: List<ScoreArchive>,
)
