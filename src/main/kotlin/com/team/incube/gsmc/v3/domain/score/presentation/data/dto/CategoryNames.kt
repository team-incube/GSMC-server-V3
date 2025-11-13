package com.team.incube.gsmc.v3.domain.score.presentation.data.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CategoryNames(
    @param:Schema(description = "인증제 항목 영문명", example = "VOLUNTEER")
    val englishName: String,
    @param:Schema(description = "인증제 항목 국문명", example = "봉사활동")
    val koreanName: String,
)
