package com.team.incube.gsmc.v3.domain.auth.dto

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import java.time.LocalDateTime

data class TokenPair(
    val accessToken: String,
    val accessTokenExpiresAt: LocalDateTime,
    val refreshToken: String,
    val refreshTokenExpiresAt: LocalDateTime,
    val role: MemberRole,
)
