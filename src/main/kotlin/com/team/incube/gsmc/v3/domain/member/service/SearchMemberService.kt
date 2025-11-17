package com.team.incube.gsmc.v3.domain.member.service

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.dto.constant.SortDirection
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse
import org.springframework.data.domain.Pageable

interface SearchMemberService {
    fun execute(
        email: String?,
        name: String?,
        role: MemberRole?,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        sort: SortDirection,
        pageable: Pageable,
    ): SearchMemberResponse
}
