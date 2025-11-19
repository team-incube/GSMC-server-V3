package com.team.incube.gsmc.v3.domain.category.presentation.data.response

import com.team.incube.gsmc.v3.domain.category.presentation.data.dto.CategoryItem
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "인증제 항목 검색 응답")
data class GetCategoriesResponse(
    @param:Schema(description = "인증제 항목 목록")
    val categories: List<CategoryItem>,
)
