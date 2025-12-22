package com.team.incube.gsmc.v3.domain.auth.presentation.data.response

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class GetTeacherSignUpRequestResponse(
    @Schema(description = "회원 ID")
    val memberId: Long,
    @Schema(description = "이름")
    val name: String,
    @Schema(description = "이메일")
    val email: String,
    @Schema(description = "요청한 권한 (TEACHER 또는 HOMEROOM_TEACHER)")
    val requestedRole: MemberRole,
    @Schema(description = "학년 (담임선생님의 경우만)")
    val grade: Int?,
    @Schema(description = "반 (담임선생님의 경우만)")
    val classNumber: Int?,
    @Schema(description = "요청 시각")
    val requestedAt: Instant,
)
