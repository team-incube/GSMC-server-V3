package com.team.incube.gsmc.v3.domain.member.presentation.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

class SearchMemberResponse(
    val id: String,
    val name: String,
    val email: String,
    val grade: Int?,
    @JsonProperty("class")
    val classNumber: Int?,
    val number: Int?,
    val role: MemberRole,
) {
    companion object {
        fun from(member: Member): SearchMemberResponse =
            SearchMemberResponse(
                id = member.id.toString(),
                name = member.name,
                email = member.email,
                grade = member.grade,
                classNumber = member.classNumber,
                number = member.number,
                role = member.role,
            )
    }
}
