package com.team.incube.gsmc.v3.domain.member.service.impl

import com.team.incube.gsmc.v3.domain.member.presentation.data.response.GetMemberResponse
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.member.service.FindMemberByIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMemberByIdServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : FindMemberByIdService {
    override fun execute(memberId: Long): GetMemberResponse =
        transaction {
            val member =
                memberExposedRepository.findById(memberId)
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            GetMemberResponse(
                id = member.id,
                name = member.name,
                email = member.email,
                grade = member.grade,
                classNumber = member.classNumber,
                number = member.number,
                role = member.role,
            )
        }
}
