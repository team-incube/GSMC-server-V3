package com.team.incube.gsmc.v3.domain.auth.presentation.data.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AuthTokenResponse(
    @param:Schema(description = "Access Token")
    val accessToken: String,
    @param:Schema(description = "Access Token 유효기간", example = "2025-11-12T10:12:12.123")
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val accessTokenExpiresAt: LocalDateTime,
    @param:Schema(description = "Refresh Token")
    val refreshToken: String,
    @param:Schema(description = "Refresh Token 유효기간", example = "2025-11-19T10:12:12.123")
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val refreshTokenExpiresAt: LocalDateTime,
    @param:Schema(description = "사용자 권한")
    val role: MemberRole,
)
