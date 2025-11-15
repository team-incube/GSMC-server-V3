package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateJlptScoreRequest(
    @param:Schema(description = "JLPT 등급 (1=N1, 2=N2, 3=N3, 4=N4, 5=N5)", example = "1")
    @field:Min(value = 1)
    @field:Max(value = 5)
    @field:NotNull
    val grade: Int,
    @param:Schema(description = "업로드된 파일의 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
