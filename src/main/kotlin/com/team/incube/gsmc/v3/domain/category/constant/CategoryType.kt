package com.team.incube.gsmc.v3.domain.category.constant

import com.team.incube.gsmc.v3.domain.category.constant.ScoreCalculationType
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException

enum class CategoryType(
    val englishName: String,
    val koreanName: String,
    val weight: Int?,
    val maxRecordCount: Int,
    val isAccumulated: Boolean,
    val evidenceType: EvidenceType,
    val calculationType: ScoreCalculationType,
    val isForeignLanguage: Boolean,
) {
    CERTIFICATE(
        englishName = "CERTIFICATE",
        koreanName = "자격증",
        weight = 2,
        maxRecordCount = 7,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.COUNT_BASED,
        isForeignLanguage = false,
    ),
    TOPCIT(
        englishName = "TOPCIT",
        koreanName = "TOPCIT",
        weight = null,
        maxRecordCount = 1,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = false,
    ),
    TOEIC(
        englishName = "TOEIC",
        koreanName = "TOEIC",
        weight = null,
        maxRecordCount = 1,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = true,
    ),
    JLPT(
        englishName = "JLPT",
        koreanName = "JLPT",
        weight = null,
        maxRecordCount = 1,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = true,
    ),
    TOEIC_ACADEMY(
        englishName = "TOEIC_ACADEMY",
        koreanName = "토익사관학교",
        weight = 1,
        maxRecordCount = 1,
        isAccumulated = false,
        evidenceType = EvidenceType.UNREQUIRED,
        calculationType = ScoreCalculationType.COUNT_BASED,
        isForeignLanguage = true,
    ),
    READ_A_THON(
        englishName = "READ_A_THON",
        koreanName = "빛고을독서마라톤",
        weight = null,
        maxRecordCount = 1,
        isAccumulated = false,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = false,
    ),
    VOLUNTEER(
        englishName = "VOLUNTEER",
        koreanName = "봉사활동",
        weight = null,
        maxRecordCount = 1,
        isAccumulated = false,
        evidenceType = EvidenceType.UNREQUIRED,
        calculationType = ScoreCalculationType.SCORE_BASED,
        isForeignLanguage = false,
    ),
    NCS(
        englishName = "NCS",
        koreanName = "직업기초능력평가",
        weight = null,
        maxRecordCount = 1,
        isAccumulated = false,
        evidenceType = EvidenceType.FILE,
        calculationType = ScoreCalculationType.SCORE_BASED,
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
