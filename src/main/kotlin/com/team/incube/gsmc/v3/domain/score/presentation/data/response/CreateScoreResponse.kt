package com.team.incube.gsmc.v3.domain.score.presentation.data.response

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import io.swagger.v3.oas.annotations.media.Schema

data class CreateScoreResponse(
    @param:Schema(description = "생성된 점수 ID", example = "1")
    val scoreId: Long,
    val categoryNames: CategoryNames,
    @param:Schema(description = "인증제 점수 상태", example = "PENDING")
    val scoreStatus: ScoreStatus,
    @param:Schema(description = "참여 활동명", example = "정보처리기능사")
    val activityName: String?,
)
