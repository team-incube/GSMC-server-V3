package com.team.incube.gsmc.v3.domain.member.service.impl

import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.member.service.CurrentMemberService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CurrentMemberServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : CurrentMemberService {
    override fun execute(memberId: Long): SearchMemberResponse =
        transaction {
            val member =
                memberExposedRepository.findById(memberId)
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)

            SearchMemberResponse.from(member)
        }
}
