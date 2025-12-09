package com.team.incube.gsmc.v3.domain.developer.service

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

interface UpdateMemberRoleService {
    fun execute(
        email: String,
        role: MemberRole,
    )
}
