package com.team.incube.gsmc.v3.service.member

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.service.impl.FindMyMemberServiceImpl
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class FindMyMemberServiceTest :
    BehaviorSpec({
        data class TestData(
            val currentMemberProvider: CurrentMemberProvider,
            val service: FindMyMemberServiceImpl,
        )

        fun ctx(): TestData {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val service = FindMyMemberServiceImpl(currentMemberProvider)
            return TestData(currentMemberProvider, service)
        }

        beforeTest {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                secondArg<Transaction.() -> Any>().invoke(mockk(relaxed = true))
            }
        }

        afterTest {
            unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        }

        Given("현재 로그인한 사용자 정보를 조회할 때") {
            val c = ctx()
            val member =
                Member(
                    id = 1L,
                    name = "홍길동",
                    email = "hong@gsm.hs.kr",
                    grade = 2,
                    classNumber = 1,
                    number = 5,
                    role = MemberRole.STUDENT,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member

            When("execute를 호출하면") {
                val res = c.service.execute()

                Then("현재 사용자 정보가 반환된다") {
                    res.id shouldBe 1L
                    res.name shouldBe "홍길동"
                    res.email shouldBe "hong@gsm.hs.kr"
                    res.grade shouldBe 2
                    res.classNumber shouldBe 1
                    res.number shouldBe 5
                    res.role shouldBe MemberRole.STUDENT
                }
            }
        }

        Given("교사가 로그인했을 때") {
            val c = ctx()
            val member =
                Member(
                    id = 100L,
                    name = "김선생",
                    email = "teacher@gsm.hs.kr",
                    grade = 0,
                    classNumber = 0,
                    number = 0,
                    role = MemberRole.TEACHER,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member

            When("execute를 호출하면") {
                val res = c.service.execute()

                Then("교사 정보가 반환된다") {
                    res.id shouldBe 100L
                    res.name shouldBe "김선생"
                    res.email shouldBe "teacher@gsm.hs.kr"
                    res.role shouldBe MemberRole.TEACHER
                }
            }
        }

        Given("관리자가 로그인했을 때") {
            val c = ctx()
            val member =
                Member(
                    id = 999L,
                    name = "관리자",
                    email = "admin@gsm.hs.kr",
                    grade = 0,
                    classNumber = 0,
                    number = 0,
                    role = MemberRole.ROOT,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member

            When("execute를 호출하면") {
                val res = c.service.execute()

                Then("관리자 정보가 반환된다") {
                    res.id shouldBe 999L
                    res.name shouldBe "관리자"
                    res.email shouldBe "admin@gsm.hs.kr"
                    res.role shouldBe MemberRole.ROOT
                }
            }
        }
    })
