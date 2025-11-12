package com.team.incube.gsmc.v3.domain.member.presentation.data.response

import com.team.incube.gsmc.v3.domain.member.dto.Member

data class SearchMemberResponse(
    val totalPage: Int,
    val totalElements: Long,
    val data: List<Member>,
)
