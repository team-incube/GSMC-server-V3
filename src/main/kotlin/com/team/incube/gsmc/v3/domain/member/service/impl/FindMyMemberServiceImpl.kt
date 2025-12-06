package com.team.incube.gsmc.v3.domain.member.service.impl

import com.team.incube.gsmc.v3.domain.member.presentation.data.response.GetMemberResponse
import com.team.incube.gsmc.v3.domain.member.service.FindMyMemberService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyMemberServiceImpl(
    private val currentMemberService: CurrentMemberProvider,
) : FindMyMemberService {
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
