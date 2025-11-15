package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateJlptScoreRequest(
    @param:Schema(description = "JLPT 등급 (N1, N2, N3, N4, N5)", example = "N1")
    @field:NotBlank
    @field:Pattern(regexp = "^N[1-5]$", message = "JLPT 등급은 N1, N2, N3, N4, N5 중 하나여야 합니다")
    val grade: String,
    @param:Schema(description = "업로드된 파일의 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
