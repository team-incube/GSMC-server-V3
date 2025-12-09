package com.team.incube.gsmc.v3.domain.developer.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "이메일로 사용자 삭제 요청")
data class DeleteMemberByEmailServiceRequest(
    @param:Schema(description = "삭제할 사용자 이메일", example = "user@example.com")
    @field:NotBlank
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String,
)
