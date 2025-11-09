package com.team.incube.gsmc.v3.domain.developer.repository

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

interface DeveloperExposedRepository {
    fun updateMemberRoleByEmail(
        email: String,
        role: MemberRole,
    ): Int

    fun deleteMemberByEmail(email: String): Int
}
