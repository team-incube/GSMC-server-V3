package com.team.incube.gsmc.v3.domain.category.presentation.data.response

import com.team.incube.gsmc.v3.domain.category.presentation.data.dto.CategoryItem
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "카테고리 검색 응답")
data class GetCategoriesResponse(
    @field:Schema(description = "카테고리 목록")
    val categories: List<CategoryItem>,
)
