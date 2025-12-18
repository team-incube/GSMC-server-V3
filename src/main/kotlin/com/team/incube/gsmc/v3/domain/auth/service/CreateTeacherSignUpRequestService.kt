package com.team.incube.gsmc.v3.domain.auth.service

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole

interface CreateTeacherSignUpRequestService {
    fun execute(
        name: String,
        requestedRole: MemberRole,
        grade: Int?,
        classNumber: Int?,
    )
}
