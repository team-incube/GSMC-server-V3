package com.team.incube.gsmc.v3.domain.category.presentation.data.dto

import com.team.incube.gsmc.v3.domain.category.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.category.constant.ScoreCalculationType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "인증제 항목 응답")
data class CategoryItem(
    @param:Schema(description = "인증제 항목 영문명", example = "CERTIFICATE")
    val englishName: String,
    @param:Schema(description = "인증제 항목 한글명", example = "자격증")
    val koreanName: String,
    @param:Schema(description = "가중치", example = "2")
    val weight: Int?,
    @param:Schema(description = "최대 레코드 개수", example = "7")
    val maxRecordCount: Int,
    @param:Schema(description = "누적 여부", example = "true")
    val isAccumulated: Boolean,
    @param:Schema(description = "증빙자료 타입", example = "FILE")
    val evidenceType: EvidenceType,
    @param:Schema(description = "점수 계산 타입", example = "COUNT_BASED")
    val calculationType: ScoreCalculationType,
    @param:Schema(description = "외국어 인증제 항목 여부", example = "false")
    val isForeignLanguage: Boolean,
)
