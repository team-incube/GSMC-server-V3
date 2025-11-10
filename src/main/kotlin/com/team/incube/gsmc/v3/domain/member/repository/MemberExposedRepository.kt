package com.team.incube.gsmc.v3.domain.member.repository

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.presentation.data.request.SearchMemberRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MemberExposedRepository {
    fun findMembers(
        condition: SearchMemberRequest,
        pageable: Pageable,
    ): Page<Member>

    fun findById(memberId: Long): Member?
}
