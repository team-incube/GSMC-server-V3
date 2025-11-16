package com.team.incube.gsmc.v3.domain.score.presentation.data.response

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.ScoreItem
import io.swagger.v3.oas.annotations.media.Schema

data class GetMyScoresResponse(
    @param:Schema(description = "점수 목록")
    val scores: List<ScoreItem>,
)
