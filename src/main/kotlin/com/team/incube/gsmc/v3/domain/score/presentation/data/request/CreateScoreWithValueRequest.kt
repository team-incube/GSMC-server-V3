package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class CreateScoreWithValueRequest(
    @param:Schema(
        description = "점수/등급/이름 값",
        example = "850",
    )
    @field:NotBlank
    val value: String,
)