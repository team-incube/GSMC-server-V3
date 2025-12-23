package com.team.incube.gsmc.v3.domain.auth.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class OAuthCodeRequest(
    @param:Schema(description = "OAuth Provider로부터 받은 Authorization Code")
    @field:NotBlank(message = "Authorization Code는 필수입니다.")
    val code: String,
    @param:Schema(description = "OAuth Redirect URI", example = "http://localhost:3000/callback")
    @field:NotBlank(message = "Redirect URI는 필수입니다.")
    @field:Pattern(
        regexp = "^https?://[a-zA-Z0-9.-]+(:[0-9]{1,5})?(/.*)?$",
        message = "올바른 URL 형식이어야 합니다.",
    )
    val redirectUri: String,
)
