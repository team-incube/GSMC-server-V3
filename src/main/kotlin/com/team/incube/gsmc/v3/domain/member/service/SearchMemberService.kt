package com.team.incube.gsmc.v3.domain.member.service

import com.team.incube.gsmc.v3.domain.member.presentation.data.request.SearchMemberRequest
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse

interface SearchMemberService {
    fun execute(memberId: SearchMemberRequest): List<SearchMemberResponse>
}
