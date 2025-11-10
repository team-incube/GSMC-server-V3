package com.team.incube.gsmc.v3.domain.member.repository

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

interface MemberExposedRepository {
    fun existsByIdIn(memberIds: List<Long>): Boolean

    fun updateMemberRoleByEmail(
        email: String,
        role: MemberRole,
    ): Int

    fun deleteMemberByEmail(email: String): Int
}
