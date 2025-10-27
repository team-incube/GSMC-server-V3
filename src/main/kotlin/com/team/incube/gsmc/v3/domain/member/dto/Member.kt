package com.team.incube.gsmc.v3.domain.member.dto

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

data class Member(
    val id: Long,
    val name: String,
    val email: String,
    val grade: Int?,
    val classNumber: Int?,
    val number: Int?,
    val role: MemberRole,
)
