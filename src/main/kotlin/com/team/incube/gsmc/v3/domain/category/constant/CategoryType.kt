package com.team.incube.gsmc.v3.domain.category.constant

enum class CategoryType(
    val englishName: String,
    val koreanName: String,
    val weight: Int,
    val maximumValue: Int,
    val isAccumulated: Boolean,
    val evidenceType: EvidenceType,
) {
    VOLUNTEER(
        englishName = "VOLUNTEER",
        koreanName = "봉사활동",
        weight = 10,
        maximumValue = 100,
        isAccumulated = true,
        evidenceType = EvidenceType.EVIDENCE,
    ),
    CERTIFICATE(
        englishName = "CERTIFICATE",
        koreanName = "자격증",
        weight = 15,
        maximumValue = 150,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
    ),
    COMPETITION(
        englishName = "COMPETITION",
        koreanName = "대회",
        weight = 20,
        maximumValue = 200,
        isAccumulated = true,
        evidenceType = EvidenceType.FILE,
    ),
    CLUB_ACTIVITY(
        englishName = "CLUB_ACTIVITY",
        koreanName = "동아리 활동",
        weight = 5,
        maximumValue = 50,
        isAccumulated = false,
        evidenceType = EvidenceType.EVIDENCE,
    ),
    ;

    companion object {
        fun fromEnglishName(englishName: String): CategoryType =
            entries.firstOrNull { it.englishName.equals(englishName, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid category english name: $englishName")

        fun fromKoreanName(koreanName: String): CategoryType =
            entries.firstOrNull { it.koreanName == koreanName }
                ?: throw IllegalArgumentException("Invalid category korean name: $koreanName")

        fun getAllCategories(): List<CategoryType> = entries
    }
}
