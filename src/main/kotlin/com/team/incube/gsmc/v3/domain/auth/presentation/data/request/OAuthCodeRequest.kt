package com.team.incube.gsmc.v3.domain.auth.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class OAuthCodeRequest(
    @field:Schema(description = "OAuth Provider로부터 받은 Authorization Code")
    @field:NotBlank(message = "Authorization Code는 필수입니다.")
    val code: String,
)
