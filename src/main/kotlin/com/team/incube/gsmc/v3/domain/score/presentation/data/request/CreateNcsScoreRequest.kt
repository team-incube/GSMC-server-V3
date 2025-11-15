package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateNcsScoreRequest(
    @param:Schema(description = "과목 전체 평균 점수", example = "4.6")
    @field:Min(value = 1)
    @field:Max(value = 5)
    @field:NotNull
    val averageScore: Double,
    @param:Schema(description = "업로드된 파일의 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
