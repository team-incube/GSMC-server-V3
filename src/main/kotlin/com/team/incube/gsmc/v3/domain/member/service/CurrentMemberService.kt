package com.team.incube.gsmc.v3.domain.member.service

import com.team.incube.gsmc.v3.domain.member.presentation.data.response.FindMemberResponse

interface CurrentMemberService {
    fun execute(memberId: Long): FindMemberResponse
}
