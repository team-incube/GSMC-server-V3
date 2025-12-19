package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.auth.entity.TeacherSignUpRequestRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.CreateTeacherSignUpRequestService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CreateTeacherSignUpRequestServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
    private val memberExposedRepository: MemberExposedRepository,
    private val alertExposedRepository: AlertExposedRepository,
) : CreateTeacherSignUpRequestService {
    override fun execute(
        name: String,
        requestedRole: MemberRole,
        grade: Int?,
        classNumber: Int?,
    ) {
        if (requestedRole != MemberRole.TEACHER && requestedRole != MemberRole.HOMEROOM_TEACHER) {
            throw GsmcException(ErrorCode.INVALID_TEACHER_ROLE)
        }
        if (requestedRole == MemberRole.HOMEROOM_TEACHER && (grade == null || classNumber == null)) {
            throw GsmcException(ErrorCode.HOMEROOM_TEACHER_GRADE_CLASS_REQUIRED)
        }

        transaction {
            val currentMember = currentMemberProvider.getCurrentMember()
            val request =
                TeacherSignUpRequestRedisEntity(
                    memberId = currentMember.id,
                    name = name,
                    email = currentMember.email,
                    requestedRole = requestedRole,
                    grade = grade,
                    classNumber = classNumber,
                    requestedAt = Instant.now(),
                )
            teacherSignUpRequestRedisRepository.save(request)

            val targetMembers =
                memberExposedRepository.findAllByRoleIn(
                    listOf(MemberRole.TEACHER, MemberRole.HOMEROOM_TEACHER, MemberRole.ROOT),
                )
            targetMembers.forEach { targetMember ->
                val content = "${name}님이 ${requestedRole.getAuthority()?.removePrefix("ROLE_")} 권한 회원가입을 요청했습니다."
                alertExposedRepository.saveWithoutScore(
                    sender = currentMember,
                    receiver = targetMember,
                    alertType = AlertType.TEACHER_SIGNUP_REQUEST,
                    content = content,
                )
            }
        }
    }
}
