package com.team.incube.gsmc.v3.domain.auth.presentation.data.response

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema

data class AuthTokenResponse(
    @param:Schema(description = "사용자 권한")
    val role: MemberRole,
)
