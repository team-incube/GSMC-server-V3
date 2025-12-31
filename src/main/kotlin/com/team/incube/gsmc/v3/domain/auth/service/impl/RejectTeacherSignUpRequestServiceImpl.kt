package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.RejectTeacherSignUpRequestService
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class RejectTeacherSignUpRequestServiceImpl(
    private val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
    private val memberExposedRepository: MemberExposedRepository,
    private val alertExposedRepository: AlertExposedRepository,
) : RejectTeacherSignUpRequestService {
    override fun execute(memberId: Long) {
        val request =
            teacherSignUpRequestRedisRepository
                .findById(memberId)
                .orElseThrow { GsmcException(ErrorCode.TEACHER_SIGNUP_REQUEST_NOT_FOUND) }

        transaction {
            alertExposedRepository.deleteAllByMemberId(memberId)

            memberExposedRepository.deleteByEmail(
                memberExposedRepository.findById(memberId)?.email
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND),
            )
        }

        teacherSignUpRequestRedisRepository.deleteById(memberId)
    }
}
