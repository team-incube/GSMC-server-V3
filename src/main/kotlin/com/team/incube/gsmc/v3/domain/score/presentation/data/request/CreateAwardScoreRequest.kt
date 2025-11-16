package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateAwardScoreRequest(
    @param:Schema(description = "수상명", example = "전국 학생 소프트웨어 경진대회 금상")
    @field:NotBlank
    @field:Size(max = 255)
    val awardName: String,
    @param:Schema(description = "증빙 파일 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
