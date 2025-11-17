package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "점수 거절 요청")
data class RejectScoreRequest(
    @field:Schema(description = "거절 사유", example = "증빙자료가 부족합니다")
    @field:NotBlank(message = "거절 사유는 필수입니다")
    val rejectionReason: String,
)