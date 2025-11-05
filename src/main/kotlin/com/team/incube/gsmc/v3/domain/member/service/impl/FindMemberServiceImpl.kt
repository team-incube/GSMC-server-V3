package com.team.incube.gsmc.v3.domain.member.service.impl

import com.team.incube.gsmc.v3.domain.member.presentation.data.request.FindMemberRequest
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.FindMemberResponse
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.member.service.FindMemberService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMemberServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : FindMemberService {
    override fun execute(memberId: FindMemberRequest): List<FindMemberResponse> =
        transaction {
            val member = memberExposedRepository.findMembers(memberId)
            FindMemberResponse.fromList(member)
        }
}
