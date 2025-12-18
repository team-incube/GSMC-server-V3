package com.team.incube.gsmc.v3.domain.auth.presentation.data.request

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateTeacherSignUpRequest(
    @param:Schema(description = "이름입니다.")
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
    @param:Schema(description = "요청할 권한입니다. (TEACHER 또는 HOMEROOM_TEACHER)")
    @field:NotNull(message = "권한은 필수입니다.")
    val requestedRole: MemberRole,
    @param:Schema(description = "학년입니다. (담임선생님의 경우 필수)")
    @field:Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @field:Max(value = 3, message = "학년은 3 이하여야 합니다.")
    val grade: Int?,
    @param:Schema(description = "반입니다. (담임선생님의 경우 필수)")
    @field:Min(value = 1, message = "반은 1 이상이어야 합니다.")
    @field:Max(value = 4, message = "반은 4 이하여야 합니다.")
    val classNumber: Int?,
)
