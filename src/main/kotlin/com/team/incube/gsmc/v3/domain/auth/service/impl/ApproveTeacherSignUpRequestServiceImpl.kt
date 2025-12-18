package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.ApproveTeacherSignUpRequestService
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ApproveTeacherSignUpRequestServiceImpl(
    private val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
    private val memberExposedRepository: MemberExposedRepository,
) : ApproveTeacherSignUpRequestService {
    override fun execute(memberId: Long) {
        val request =
            teacherSignUpRequestRedisRepository
                .findById(memberId)
                .orElseThrow { GsmcException(ErrorCode.TEACHER_SIGNUP_REQUEST_NOT_FOUND) }
        transaction {
            val member =
                memberExposedRepository.findById(memberId)
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            memberExposedRepository.update(
                id = memberId,
                name = request.name,
                email = member.email,
                grade = request.grade,
                classNumber = request.classNumber,
                number = null,
                role = request.requestedRole,
            )
        }
        teacherSignUpRequestRedisRepository.deleteById(memberId)
    }
}
