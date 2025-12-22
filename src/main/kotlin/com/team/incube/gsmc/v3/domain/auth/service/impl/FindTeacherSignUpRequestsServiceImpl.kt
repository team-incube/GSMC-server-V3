package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.GetTeacherSignUpRequestResponse
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.FindTeacherSignUpRequestsService
import org.springframework.stereotype.Service

@Service
class FindTeacherSignUpRequestsServiceImpl(
    private val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
) : FindTeacherSignUpRequestsService {
    override fun execute(): List<GetTeacherSignUpRequestResponse> =
        teacherSignUpRequestRedisRepository
            .findAll()
            .map { request ->
                GetTeacherSignUpRequestResponse(
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
