package com.team.incube.gsmc.v3.domain.developer.presentation.data.request

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PatchMemberRoleRequest(
    @field:NotBlank
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String,
    @field:NotNull
    val role: MemberRole,
)
