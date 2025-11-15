package com.team.incube.gsmc.v3.domain.auth.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SignUpRequest(
    @field:Schema(description = "이름입니다.")
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
    @field:Schema(description = "학번입니다.")
    @field:NotNull(message = "학번은 필수입니다.")
    val studentNumber: Int,
)
