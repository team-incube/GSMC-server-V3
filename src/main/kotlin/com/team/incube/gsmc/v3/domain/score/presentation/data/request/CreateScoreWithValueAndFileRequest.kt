package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateScoreWithValueAndFileRequest(
    @param:Schema(
        description = "점수/등급/이름 값",
        example = "850",
    )
    @field:NotBlank
    @field:Size(max = 255, message = "값은 최대 255자까지 가능합니다")
    val value: String,
    @param:Schema(description = "증빙 파일 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
