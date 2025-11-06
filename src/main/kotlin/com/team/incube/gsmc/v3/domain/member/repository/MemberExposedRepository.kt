package com.team.incube.gsmc.v3.domain.member.repository

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.presentation.data.request.SearchMemberRequest

interface MemberExposedRepository {
    fun findMembers(query: SearchMemberRequest): List<Member>

    fun findById(memberId: Long): Member?
}
