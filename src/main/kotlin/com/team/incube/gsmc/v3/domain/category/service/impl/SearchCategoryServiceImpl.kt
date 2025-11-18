package com.team.incube.gsmc.v3.domain.category.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.category.presentation.data.dto.CategoryItem
import com.team.incube.gsmc.v3.domain.category.presentation.data.response.GetCategoriesResponse
import com.team.incube.gsmc.v3.domain.category.service.SearchCategoryService
import org.springframework.stereotype.Service

@Service
class SearchCategoryServiceImpl : SearchCategoryService {
    override fun execute(keyword: String?): GetCategoriesResponse {
        val categories =
            CategoryType
                .getAllCategories()
                .filter {
                    keyword.isNullOrBlank() ||
                        it.koreanName.contains(keyword, ignoreCase = true) ||
                        it.englishName.contains(keyword, ignoreCase = true)
                }.map { categoryType ->
                    CategoryItem(
                        englishName = categoryType.englishName,
                        koreanName = categoryType.koreanName,
                        weight = categoryType.weight,
                        maxRecordCount = categoryType.maxRecordCount,
                        isAccumulated = categoryType.isAccumulated,
                        evidenceType = categoryType.evidenceType,
                        calculationType = categoryType.calculationType,
                        isForeignLanguage = categoryType.isForeignLanguage,
                    )
                }

        return GetCategoriesResponse(categories = categories)
    }
}
