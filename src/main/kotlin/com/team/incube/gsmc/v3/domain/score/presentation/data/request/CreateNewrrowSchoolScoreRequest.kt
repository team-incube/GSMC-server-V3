package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateNewrrowSchoolScoreRequest(
    @param:Schema(description = "회고온도", example = "80")
    @field:NotNull
    @field:Min(value = 0)
    @field:Max(value = 100)
    val temperature: Int,
    @param:Schema(description = "증빙 파일 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
