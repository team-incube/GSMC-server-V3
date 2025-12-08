package com.team.incube.gsmc.v3.service.developer

import com.team.incube.gsmc.v3.domain.developer.service.impl.DeleteMemberByEmailServiceImpl
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class DeleteMemberByEmailServiceTest :
    BehaviorSpec({
        data class TestData(
            val memberRepo: MemberExposedRepository,
            val service: DeleteMemberByEmailServiceImpl,
        )

        fun ctx(): TestData {
            val memberRepo = mockk<MemberExposedRepository>()
            val service = DeleteMemberByEmailServiceImpl(memberRepo)
            return TestData(memberRepo, service)
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

        Given("존재하는 이메일로 회원을 삭제할 때") {
            val c = ctx()
            val email = "test@test.com"

            every { c.memberRepo.deleteMemberByEmail(email) } returns 1

            When("execute를 호출하면") {
                c.service.execute(email)

                Then("회원이 삭제된다") {
                    verify(exactly = 1) { c.memberRepo.deleteMemberByEmail(email) }
                }
            }
        }

        Given("존재하지 않는 이메일로 회원을 삭제하려고 할 때") {
            val c = ctx()
            val email = "none@test.com"

            every { c.memberRepo.deleteMemberByEmail(email) } returns 0

            When("execute를 호출하면") {
                Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(email) }
                    ex.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                }
            }
        }
    })
