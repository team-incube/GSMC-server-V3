package com.team.incube.gsmc.v3.domain.category.dto

import com.team.incube.gsmc.v3.domain.category.dto.constant.EvidenceType

data class Category(
    val id: Long,
    val englishName: String,
    val koreanName: String,
    val weight: Int,
    val maximumValue: Int,
    val isAccumulated: Boolean,
    val evidenceType: EvidenceType,
)
