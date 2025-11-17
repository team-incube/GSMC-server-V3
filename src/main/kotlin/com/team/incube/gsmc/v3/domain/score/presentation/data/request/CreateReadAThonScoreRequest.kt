package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateReadAThonScoreRequest(
    @param:Schema(description = "독서마라톤 단계 (1=거북이, 2=악어, 3=토끼, 4=타조, 5=사자, 6=호랑이, 7=월계관)", example = "2")
    @field:Min(value = 1)
    @field:Max(value = 7)
    @field:NotNull
    val grade: Int,
    @param:Schema(description = "업로드된 파일의 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
