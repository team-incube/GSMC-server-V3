package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.service.impl.FindEvidenceDraftServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class FindEvidenceDraftServiceTest :
    BehaviorSpec({
        data class Ctx(
            val currentMemberProvider: CurrentMemberProvider,
            val service: FindEvidenceDraftServiceImpl,
        )

        fun ctx(): Ctx {
            val c = mockk<CurrentMemberProvider>()
            val s = FindEvidenceDraftServiceImpl(c)
            return Ctx(c, s)
        }

        Given("증빙자료 임시저장 조회 요청이 주어지면") {
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

            When("임시저장을 조회하면") {
                val result = c.service.execute()

                Then("null이 반환된다 (캐시에 저장된 데이터가 없는 경우)") {
                    result shouldBe null
                }
            }
        }

        Given("다른 사용자의 임시저장 조회 요청이 주어지면") {
            val c = ctx()
            val member =
                Member(
                    id = 2L,
                    name = "김철수",
                    email = "kim@gsm.hs.kr",
                    grade = 1,
                    classNumber = 2,
                    number = 3,
                    role = MemberRole.STUDENT,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member

            When("임시저장을 조회하면") {
                val result = c.service.execute()

                Then("null이 반환된다") {
                    result shouldBe null
                }
            }
        }

        Given("선생님이 임시저장을 조회하면") {
            val c = ctx()
            val teacher =
                Member(
                    id = 999L,
                    name = "선생님",
                    email = "teacher@gsm.hs.kr",
                    grade = 3,
                    classNumber = 4,
                    number = 1,
                    role = MemberRole.TEACHER,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns teacher

            When("임시저장을 조회하면") {
                val result = c.service.execute()

                Then("null이 반환된다") {
                    result shouldBe null
                }
            }
        }
    })
