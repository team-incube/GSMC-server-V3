package com.team.incube.gsmc.v3.domain.score.presentation.data.response

import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryScoreGroup
import io.swagger.v3.oas.annotations.media.Schema

data class GetScoresByCategoryResponse(
    @param:Schema(description = "카테고리별 점수 그룹 목록")
    val categories: List<CategoryScoreGroup>,
)