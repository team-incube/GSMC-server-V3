package com.team.incube.gsmc.v3.domain.category.constant

import com.team.incube.gsmc.v3.domain.category.constant.ScoreCalculationType
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException

enum class CategoryType(
    val englishName: String,
    val koreanName: String,
    val weight: Int,
    val maximumValue: Int,
    val isAccumulated: Boolean,
    val evidenceType: EvidenceType,
    val calculationType: ScoreCalculationType,
    val isForeignLanguage: Boolean,
) {
    VOLUNTEER(
        englishName = "VOLUNTEER",
        koreanName = "봉사활동",
        weight = 10,
        maximumValue = 100,
        isAccumulated = true,
        evidenceType = EvidenceType.EVIDENCE,
        calculationType = ScoreCalculationType.COUNT_BASED,
        isForeignLanguage = false,
    ),
    CERTIFICATE(
        englishName = "CERTIFICATE",
        koreanName = "자격증",
        weight = 2,
        maximumValue = 7,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.COUNT_BASED,
        isForeignLanguage = false,
    ),
    TOPCIT(
        englishName = "TOPCIT",
        koreanName = "TOPCIT",
        weight = 1,
        maximumValue = 10,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = false,
    ),
    TOEIC(
        englishName = "TOEIC",
        koreanName = "TOEIC",
        weight = 1,
        maximumValue = 10,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = true,
    ),
    JLPT(
        englishName = "JLPT",
        koreanName = "JLPT",
        weight = 1,
        maximumValue = 10,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = true,
    ),
    TOEIC_ACADEMY(
        englishName = "TOEIC_ACADEMY",
        koreanName = "토익사관학교",
        weight = 1,
        maximumValue = 1,
        isAccumulated = false,
        evidenceType = EvidenceType.UNREQUIRED,
        calculationType = ScoreCalculationType.COUNT_BASED,
        isForeignLanguage = true,
    ),
    CLUB_ACTIVITY(
        englishName = "CLUB_ACTIVITY",
        koreanName = "동아리 활동",
        weight = 5,
        maximumValue = 50,
        isAccumulated = false,
        evidenceType = EvidenceType.EVIDENCE,
        calculationType = ScoreCalculationType.COUNT_BASED,
        isForeignLanguage = false,
    ),
    ;

    companion object {
        fun fromEnglishName(englishName: String): CategoryType =
            entries.firstOrNull { it.englishName.equals(englishName, ignoreCase = true) }
                ?: throw GsmcException(ErrorCode.INVALID_CATEGORY)

        fun fromKoreanName(koreanName: String): CategoryType =
            entries.firstOrNull { it.koreanName == koreanName }
                ?: throw GsmcException(ErrorCode.INVALID_CATEGORY)

        fun getAllCategories(): List<CategoryType> = entries

        fun getForeignLanguageCategories(): List<CategoryType> = entries.filter { it.isForeignLanguage }

        fun exists(englishName: String): Boolean = entries.any { it.englishName.equals(englishName, ignoreCase = true) }
    }
}
