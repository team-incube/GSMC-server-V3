package com.team.incube.gsmc.v3.domain.member.service.impl

import com.team.incube.gsmc.v3.domain.member.presentation.data.response.GetMemberResponse
import com.team.incube.gsmc.v3.domain.member.service.GetCurrentMemberService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class GetCurrentMemberServiceImpl(
    private val currentMemberService: CurrentMemberProvider,
) : GetCurrentMemberService {
    override fun execute(): GetMemberResponse =
        transaction {
            currentMemberService.getCurrentMember().run {
                GetMemberResponse(
                    id = id,
                    name = name,
                    email = email,
                    grade = grade,
                    classNumber = classNumber,
                    number = number,
                    role = role,
                )
            }
        }
}
