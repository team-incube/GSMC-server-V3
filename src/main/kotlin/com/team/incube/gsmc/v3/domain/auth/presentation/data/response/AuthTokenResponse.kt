package com.team.incube.gsmc.v3.domain.auth.presentation.data.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthTokenResponse(
    @param:Schema(description = "사용자 권한", required = true)
    val role: MemberRole,
    @param:Schema(description = "Access Token (Cookie로 전송됨)", hidden = true)
    val accessToken: String? = null,
    @param:Schema(description = "Refresh Token (Cookie로 전송됨)", hidden = true)
    val refreshToken: String? = null,
    @param:Schema(description = "Access Token 만료 시간", hidden = true)
    val accessExpiration: LocalDateTime? = null,
    @param:Schema(description = "Refresh Token 만료 시간", hidden = true)
    val refreshExpiration: LocalDateTime? = null,
)
