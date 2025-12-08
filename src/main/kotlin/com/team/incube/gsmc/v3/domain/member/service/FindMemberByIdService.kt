package com.team.incube.gsmc.v3.domain.member.service

import com.team.incube.gsmc.v3.domain.member.presentation.data.response.GetMemberResponse

interface FindMemberByIdService {
    fun execute(memberId: Long): GetMemberResponse
}
