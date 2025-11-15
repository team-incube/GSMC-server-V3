package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.service.SignUpService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
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
            val member = currentMemberProvider.getCurrentUser()

            val grade = studentNumber / 1000
            val classNumber = (studentNumber / 100) % 10
            val number = studentNumber % 100

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
}
