package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateToeicScoreRequest(
    @param:Schema(description = "TOEIC 점수", example = "850")
    @field:Min(value = 10)
    @field:Max(value = 990)
    @field:NotNull
    val value: Int,
    @param:Schema(description = "업로드된 파일의 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
