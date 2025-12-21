package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.auth.service.impl.SignUpServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class SignUpServiceTest :
    BehaviorSpec({

        data class TestContext(
            val currentMemberProvider: CurrentMemberProvider,
            val memberExposedRepository: MemberExposedRepository,
            val service: SignUpServiceImpl,
        )

        fun ctx(): TestContext {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val memberExposedRepository = mockk<MemberExposedRepository>()

            val service =
                SignUpServiceImpl(
                    currentMemberProvider = currentMemberProvider,
                    memberExposedRepository = memberExposedRepository,
                )

            return TestContext(
                currentMemberProvider,
                memberExposedRepository,
                service,
            )
        }

        val mockTransaction = mockk<JdbcTransaction>(relaxed = true)

        mockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        every {
            org.jetbrains.exposed.v1.jdbc.transactions.transaction(
                db = null,
                statement = any<JdbcTransaction.() -> Any?>(),
            )
        } answers {
            @Suppress("UNCHECKED_CAST")
            val block = it.invocation.args.last() as JdbcTransaction.() -> Any?
            block.invoke(mockTransaction)
        }

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        Given("로그인된 사용자가 회원가입을 진행할 때") {
            val c = ctx()
            val memberId = 1L
            val email = "student@gsm.hs.kr"

            val member =
                Member(
                    id = memberId,
                    name = "기존이름",
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )

            val name = "홍길동"
            val studentNumber = 2323

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every {
                c.memberExposedRepository.update(
                    id = memberId,
                    name = name,
                    email = email,
                    grade = 2,
                    classNumber = 3,
                    number = 23,
                    role = MemberRole.STUDENT,
                )
            } returns 1

            When("execute를 호출하면") {
                c.service.execute(name, studentNumber)

                Then("회원 정보가 STUDENT 권한과 학번 정보로 업데이트된다") {
                    verify(exactly = 1) {
                        c.memberExposedRepository.update(
                            id = memberId,
                            name = name,
                            email = email,
                            grade = 2,
                            classNumber = 3,
                            number = 23,
                            role = MemberRole.STUDENT,
                        )
                    }
                }
            }
        }
    })
