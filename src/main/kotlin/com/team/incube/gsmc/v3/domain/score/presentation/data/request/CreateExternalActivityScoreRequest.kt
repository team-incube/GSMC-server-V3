package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateExternalActivityScoreRequest(
    @param:Schema(description = "외부활동명", example = "해커톤 참여")
    @field:NotBlank
    val activityName: String,
    @param:Schema(description = "파일 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
