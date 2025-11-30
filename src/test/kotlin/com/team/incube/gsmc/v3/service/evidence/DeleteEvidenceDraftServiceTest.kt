package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.service.impl.DeleteMyEvidenceDraftServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class DeleteEvidenceDraftServiceTest :
    BehaviorSpec({
        data class Ctx(
            val currentMemberProvider: CurrentMemberProvider,
            val service: DeleteMyEvidenceDraftServiceImpl,
        )

        fun ctx(): Ctx {
            val c = mockk<CurrentMemberProvider>()
            val s = DeleteMyEvidenceDraftServiceImpl(c)
            return Ctx(c, s)
        }

        Given("증빙자료 임시저장 삭제 요청이 주어지면") {
            val c = ctx()
            val member =
                Member(
                    id = 1L,
                    name = "홍길동",
                    email = "test@gsm.hs.kr",
                    grade = 2,
                    classNumber = 1,
                    number = 5,
                    role = MemberRole.STUDENT,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member

            When("execute를 호출하면") {
                val result = c.service.execute()

                Then("예외 없이 정상적으로 실행되고 캐시가 삭제된다") {
                    result shouldBe Unit
                }
            }
        }
    })
