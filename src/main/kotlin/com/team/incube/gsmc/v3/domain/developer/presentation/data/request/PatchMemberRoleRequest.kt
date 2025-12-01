package com.team.incube.gsmc.v3.domain.developer.presentation.data.request

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "사용자 권한 수정 요청")
data class PatchMemberRoleRequest(
    @param:Schema(description = "사용자 이메일", example = "user@example.com")
    @field:NotBlank
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String,
    @param:Schema(description = "변경할 권한", example = "ADMIN")
    @field:NotNull
    val role: MemberRole,
)
