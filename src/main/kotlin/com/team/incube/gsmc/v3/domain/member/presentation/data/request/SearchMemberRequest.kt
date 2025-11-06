package com.team.incube.gsmc.v3.domain.member.presentation.data.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

data class SearchMemberRequest(
    val email: String? = null,
    val name: String? = null,
    val role: MemberRole? = null,
    val grade: Int? = null,
    @JsonProperty("class")
    val classNumber: Int? = null,
    val number: Int? = null,
    val maxScore: Int? = null,
    val minScore: Int? = null,
)
