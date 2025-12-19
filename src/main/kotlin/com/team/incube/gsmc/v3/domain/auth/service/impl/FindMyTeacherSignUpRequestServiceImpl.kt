package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.GetTeacherSignUpRequestResponse
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.FindMyTeacherSignUpRequestService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.stereotype.Service

@Service
class FindMyTeacherSignUpRequestServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
) : FindMyTeacherSignUpRequestService {
    override fun execute(): GetTeacherSignUpRequestResponse {
        val memberId = currentMemberProvider.getCurrentMemberId()

        val request =
            teacherSignUpRequestRedisRepository.findById(memberId).orElseThrow {
                GsmcException(ErrorCode.TEACHER_SIGNUP_REQUEST_NOT_FOUND)
            }

        return GetTeacherSignUpRequestResponse(
            memberId = request.memberId,
            name = request.name,
            email = request.email,
            requestedRole = request.requestedRole,
            grade = request.grade,
            classNumber = request.classNumber,
            requestedAt = request.requestedAt,
        )
    }
}
