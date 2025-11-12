package com.team.incube.gsmc.v3.domain.auth.presentation.data.response

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AuthTokenResponse(
    @field:Schema(description = "Access Token")
    val accessToken: String,
    @field:Schema(description = "Access Token 유효기간", example = "2025-11-12T10:12:12.123")
    val accessTokenExpiresAt: LocalDateTime,
    @field:Schema(description = "Refresh Token")
    val refreshToken: String,
    @field:Schema(description = "Refresh Token 유효기간", example = "2025-11-19T10:12:12.123")
    val refreshTokenExpiresAt: LocalDateTime,
    @field:Schema(description = "사용자 권한")
    val role: MemberRole,
)
