package com.team.incube.gsmc.v3.domain.member.service

import com.team.incube.gsmc.v3.domain.member.presentation.data.request.SearchMemberRequest
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SearchMemberService {
    fun execute(
        request: SearchMemberRequest,
        pageable: Pageable,
    ): Page<SearchMemberResponse>
}
