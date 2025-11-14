package com.team.incube.gsmc.v3.domain.category.service

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Component

@Component
class CategoryManager {
    private val categories: Map<String, CategoryType> = CategoryType.entries.associateBy { it.englishName }

    fun getByEnglishName(englishName: String): CategoryType =
        categories[englishName] ?: throw GsmcException(ErrorCode.INVALID_CATEGORY)

    fun getByKoreanName(koreanName: String): CategoryType =
        CategoryType.entries.firstOrNull { it.koreanName == koreanName }
            ?: throw GsmcException(ErrorCode.INVALID_CATEGORY)

    fun getAllCategories(): List<CategoryType> = CategoryType.entries

    fun exists(englishName: String): Boolean = categories.containsKey(englishName)
}
