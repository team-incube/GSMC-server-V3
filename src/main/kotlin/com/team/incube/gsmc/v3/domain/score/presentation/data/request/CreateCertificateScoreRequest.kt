package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateCertificateScoreRequest(
    @param:Schema(description = "자격증 이름", example = "정보처리기사")
    @field:NotBlank
    @field:Size(max = 255)
    val certificateName: String,
    @param:Schema(description = "증빙 파일 ID", example = "1")
    @field:NotNull
    val fileId: Long,
)
