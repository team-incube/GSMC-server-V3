package com.team.incube.gsmc.v3.domain.project.dto

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

data class ProjectParticipant(
    val id: Long,
    val name: String,
    val email: String,
    val grade: Int?,
    val classNumber: Int?,
    val number: Int?,
    val role: MemberRole,
    val scoreId: Long?,
)
