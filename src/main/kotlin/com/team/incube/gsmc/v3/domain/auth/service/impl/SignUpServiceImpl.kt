package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.service.SignUpService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class SignUpServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val memberExposedRepository: MemberExposedRepository,
) : SignUpService {
    override fun execute(
        name: String,
        studentNumber: Int,
    ) {
        transaction {
            val member = currentMemberProvider.getCurrentMember()

            val grade = studentNumber / 1000
            val classNumber = (studentNumber / 100) % 10
            val number = studentNumber % 100

            // 학번 범위 검증: 1101~1418, 2101~2418, 3101~3418
            if (!isValidStudentNumber(grade, classNumber, number)) {
                throw GsmcException(ErrorCode.MEMBER_INVALID_STUDENT_NUMBER)
            }

            if (memberExposedRepository.existsByGradeAndClassNumberAndNumber(
                    grade = grade,
                    classNumber = classNumber,
                    number = number,
                )
            ) {
                throw GsmcException(ErrorCode.MEMBER_STUDENT_NUMBER_ALREADY_EXISTS)
            }

            memberExposedRepository.update(
                id = member.id,
                name = name,
                email = member.email,
                grade = grade,
                classNumber = classNumber,
                number = number,
                role = MemberRole.STUDENT,
            )
        }
    }

    private fun isValidStudentNumber(
        grade: Int,
        classNumber: Int,
        number: Int,
    ): Boolean {
        // 학년: 1, 2, 3
        if (grade !in 1..3) return false

        // 반: 1, 2, 3, 4
        if (classNumber !in 1..4) return false

        // 번호: 1~18
        if (number !in 1..18) return false

        return true
    }
}
