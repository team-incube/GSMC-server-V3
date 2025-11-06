package com.team.incube.gsmc.v3.domain.member.service.impl

import com.team.incube.gsmc.v3.domain.member.presentation.data.request.SearchMemberRequest
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.member.service.SearchMemberService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class SearchMemberServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : SearchMemberService {
    override fun execute(memberId: SearchMemberRequest): List<SearchMemberResponse> =
        transaction {
            val member = memberExposedRepository.findMembers(memberId)
            SearchMemberResponse.fromList(member)
        }
}
