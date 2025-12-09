package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class UpdateScoreStatusRequest(
    @param:Schema(description = "점수 상태", example = "APPROVED", allowableValues = ["APPROVED", "REJECTED"])
    @field:NotNull(message = "점수 상태는 필수입니다")
    val scoreStatus: ScoreStatus,
)
