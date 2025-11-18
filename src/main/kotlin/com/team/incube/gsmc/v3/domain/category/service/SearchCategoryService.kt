package com.team.incube.gsmc.v3.domain.category.service

import com.team.incube.gsmc.v3.domain.category.presentation.data.response.GetCategoriesResponse

interface SearchCategoryService {
    fun execute(keyword: String?): GetCategoriesResponse
}
