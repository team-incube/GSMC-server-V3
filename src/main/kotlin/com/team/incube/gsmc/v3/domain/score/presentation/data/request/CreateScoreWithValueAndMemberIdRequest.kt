package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateScoreWithValueAndMemberIdRequest(
    @param:Schema(
        description = "점수/등급/이름 값",
        example = "850",
    )
    @field:NotBlank
    val value: String,
    @param:Schema(
        description = "학생 ID",
        example = "1",
    )
    @field:NotNull
    val memberId: Long,
)
