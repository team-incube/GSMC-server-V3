package com.team.incube.gsmc.v3.domain.member.repository

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.presentation.data.request.FindMemberRequest

interface MemberExposedRepository {

    fun findMembers(query: FindMemberRequest): List<Member>

    fun findById(memberId: Long): Member?
}
