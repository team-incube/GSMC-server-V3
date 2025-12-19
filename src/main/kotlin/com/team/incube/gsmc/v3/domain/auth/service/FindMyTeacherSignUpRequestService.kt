package com.team.incube.gsmc.v3.domain.auth.service

import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.GetTeacherSignUpRequestResponse

interface FindMyTeacherSignUpRequestService {
    fun execute(): GetTeacherSignUpRequestResponse
}
