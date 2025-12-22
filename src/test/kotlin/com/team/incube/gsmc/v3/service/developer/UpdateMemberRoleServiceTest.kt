package com.team.incube.gsmc.v3.service.developer
import com.team.incube.gsmc.v3.domain.developer.service.impl.UpdateMemberRoleServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
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
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class UpdateMemberRoleServiceTest :
    BehaviorSpec({
        data class TestData(
            val memberRepo: MemberExposedRepository,
            val service: UpdateMemberRoleServiceImpl,
        )

        fun ctx(): TestData {
            val memberRepo = mockk<MemberExposedRepository>()
            val service = UpdateMemberRoleServiceImpl(memberRepo)
            return TestData(memberRepo, service)
        }

        val mockTransaction = mockk<JdbcTransaction>(relaxed = true)

        mockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        every {
            org.jetbrains.exposed.v1.jdbc.transactions.transaction(
                db = null,
                statement = any<JdbcTransaction.() -> Any?>(),
            )
        } answers { call ->
            @Suppress("UNCHECKED_CAST")
            val block = call.invocation.args.last() as JdbcTransaction.() -> Any?
            block.invoke(mockTransaction)
        }

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        Given("존재하는 회원의 역할을 TEACHER로 변경할 때") {
            val c = ctx()
            val email = "test@test.com"
            every { c.memberRepo.updateMemberRoleByEmail(email, MemberRole.TEACHER) } returns 1
            When("execute를 호출하면") {
                c.service.execute(email, MemberRole.TEACHER)
                Then("역할이 TEACHER로 변경된다") {
                    verify(exactly = 1) { c.memberRepo.updateMemberRoleByEmail(email, MemberRole.TEACHER) }
                }
            }
        }
        Given("존재하지 않는 이메일로 역할을 변경하려고 할 때") {
            val c = ctx()
            val email = "none@test.com"
            every { c.memberRepo.updateMemberRoleByEmail(email, MemberRole.TEACHER) } returns 0
            When("execute를 호출하면") {
                Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(email, MemberRole.TEACHER) }
                    ex.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                }
            }
        }
    })
