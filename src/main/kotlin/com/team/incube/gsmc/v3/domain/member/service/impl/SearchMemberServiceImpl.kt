package com.team.incube.gsmc.v3.domain.member.service.impl

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.member.service.SearchMemberService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SearchMemberServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : SearchMemberService {
    override fun execute(
        email: String?,
        name: String?,
        role: MemberRole?,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        pageable: Pageable,
    ): SearchMemberResponse =
        transaction {
            val members =
                memberExposedRepository.searchMembers(
                    email = email,
                    name = name,
                    role = role,
                    grade = grade,
                    classNumber = classNumber,
                    number = number,
                    pageable = pageable,
                )
            SearchMemberResponse(
                totalPage = members.totalPages,
                totalElements = members.totalElements.toInt(),
                data = members.content,
            )
        }
}
