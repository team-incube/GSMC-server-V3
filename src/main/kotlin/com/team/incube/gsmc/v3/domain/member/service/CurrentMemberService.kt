package com.team.incube.gsmc.v3.domain.member.service

import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse

interface CurrentMemberService {
    fun execute(memberId: Long): SearchMemberResponse
}
