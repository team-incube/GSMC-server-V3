package com.team.incube.gsmc.v3.domain.developer.presentation.data.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class WithdrawMemberRequest(
    @field:NotBlank
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String,
)
