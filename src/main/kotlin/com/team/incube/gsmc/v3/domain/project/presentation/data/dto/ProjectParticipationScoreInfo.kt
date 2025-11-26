package com.team.incube.gsmc.v3.domain.project.presentation.data.dto

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import io.swagger.v3.oas.annotations.media.Schema

data class ProjectParticipationScoreInfo(
    @param:Schema(description = "점수 ID", example = "1")
    val scoreId: Long,
    @param:Schema(description = "카테고리 이름 정보")
    val categoryNames: CategoryNames,
    @param:Schema(description = "인증제 점수 상태", example = "PENDING")
    val scoreStatus: ScoreStatus,
    @param:Schema(description = "프로젝트명", example = "GSMC")
    val activityName: String?,
    @param:Schema(description = "점수 값", example = "95.5", nullable = true)
    val scoreValue: Double?,
    @param:Schema(description = "거절 사유", example = "증빙자료가 부족합니다", nullable = true)
    val rejectionReason: String?,
)
