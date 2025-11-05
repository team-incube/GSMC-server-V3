package com.team.incube.gsmc.v3.domain.member.service

import com.team.incube.gsmc.v3.domain.member.presentation.data.request.FindMemberRequest
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.FindMemberResponse

interface FindMemberService {
    fun execute(memberId: FindMemberRequest): List<FindMemberResponse>
}
