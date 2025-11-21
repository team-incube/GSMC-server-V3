package com.team.incube.gsmc.v3.domain.score.presentation.data.dto

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import io.swagger.v3.oas.annotations.media.Schema

data class CategoryScoreGroup(
    @param:Schema(description = "카테고리 타입", example = "TOEIC")
    val categoryType: CategoryType,
    val categoryNames: CategoryNames,
    @param:Schema(description = "환산된 인정 점수", example = "6")
    val recognizedScore: Int,
    @param:Schema(description = "해당 카테고리의 점수 목록")
    val scores: List<ScoreItem>,
)