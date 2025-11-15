package com.team.incube.gsmc.v3.domain.score.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

data class GetTotalScoreResponse(
    @Schema(description = "총점", example = "85")
    val totalScore: Int,
)
