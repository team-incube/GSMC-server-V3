package com.team.incube.gsmc.v3.domain.category.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.category.presentation.data.dto.CategoryItem
import com.team.incube.gsmc.v3.domain.category.presentation.data.response.GetCategoriesResponse
import com.team.incube.gsmc.v3.domain.category.service.FindAllCategoryService
import org.springframework.stereotype.Service

@Service
class FindAllCategoryServiceImpl : FindAllCategoryService {
    override fun execute(): GetCategoriesResponse {
        return GetCategoriesResponse(CategoryType.getAllCategories().map { categoryType ->
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
        })
    }
}
