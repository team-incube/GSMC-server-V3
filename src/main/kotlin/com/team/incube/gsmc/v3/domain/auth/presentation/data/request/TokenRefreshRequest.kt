package com.team.incube.gsmc.v3.domain.auth.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class TokenRefreshRequest(
    @field:Schema(description = "JWT Refresh Token 입니다.")
    @field:NotBlank(message = "Refresh Token은 필수입니다.")
    val refreshToken: String,
)
