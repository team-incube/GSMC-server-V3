package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateTopcitScoreRequest(
    @param:Schema(description = "TOPCIT 점수", example = "850")
    @field:Min(value = 1)
    @field:Max(value = 1000)
    @field:NotNull
    val value: Int,
    @param:Schema(description = "업로드된 파일의 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
