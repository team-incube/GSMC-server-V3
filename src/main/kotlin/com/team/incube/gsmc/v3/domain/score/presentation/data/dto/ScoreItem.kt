package com.team.incube.gsmc.v3.domain.score.presentation.data.dto

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class ScoreItem(
    @param:Schema(description = "점수 ID", example = "1")
    val scoreId: Long,
    val categoryNames: CategoryNames,
    @param:Schema(description = "인증제 점수 상태", example = "PENDING")
    val scoreStatus: ScoreStatus,
    @param:Schema(description = "참여 활동명", example = "정보처리기능사")
    val activityName: String?,
    @param:Schema(description = "점수 값", example = "95.5")
    val scoreValue: Double?,
    @param:Schema(description = "거절 사유", example = "증빙자료가 부족합니다")
    val rejectionReason: String?,
    @param:Schema(description = "최종 생성/수정 시간", example = "2025-01-15T10:30:00Z")
    val updatedAt: Instant?,
)
