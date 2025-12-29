package com.team.incube.gsmc.v3.service.alert

import com.team.incube.gsmc.v3.domain.alert.dto.Alert
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.impl.DeleteAlertServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import java.time.LocalDateTime

class DeleteAlertServiceTest :
    BehaviorSpec({
        data class TestContext(
            val alertRepo: AlertExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: DeleteAlertServiceImpl,
        )

        fun ctx(): TestContext {
            val alertRepo = mockk<AlertExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val service = DeleteAlertServiceImpl(alertRepo, currentMemberProvider)
            return TestContext(alertRepo, currentMemberProvider, service)
        }

        // 스펙 초기화 시점에 transaction mock 설정
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

        afterSpec { unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt") }

        fun currentMember() =
            Member(
                id = 1L,
                name = "학생",
                email = "student@test.com",
                grade = 1,
                classNumber = 1,
                number = 1,
                role = MemberRole.STUDENT,
            )

        fun anotherMember() =
            Member(
                id = 2L,
                name = "다른학생",
                email = "another@test.com",
                grade = 1,
                classNumber = 1,
                number = 2,
                role = MemberRole.STUDENT,
            )

        fun teacher() =
            Member(
                id = 3L,
                name = "선생님",
                email = "teacher@test.com",
                grade = 0,
                classNumber = 0,
                number = 0,
                role = MemberRole.TEACHER,
            )

        fun alert(receiver: Member = currentMember()) =
            Alert(
                id = 10L,
                sender = teacher(),
                receiver = receiver,
                score = mockk(relaxed = true),
                projectId = null,
                alertType = mockk(relaxed = true),
                isRead = false,
                content = "알림",
                createdAt = LocalDateTime.now(),
            )

        Given("본인 알림을 삭제할 때") {
            val c = ctx()
            val member = currentMember()
            val alertId = 10L
            val alertInstance = alert(receiver = member)

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findById(alertId) } returns alertInstance
            every { c.alertRepo.deleteById(alertId) } returns 1

            When("execute를 호출하면") {
                c.service.execute(alertId)

                Then("알림이 삭제된다") {
                    verify(exactly = 1) { c.alertRepo.findById(alertId) }
                    verify(exactly = 1) { c.alertRepo.deleteById(alertId) }
                }
            }
        }

        Given("알림이 존재하지 않으면") {
            val c = ctx()
            val member = currentMember()
            val alertId = 99L

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findById(alertId) } returns null

            When("execute를 호출하면") {
                Then("ALERT_NOT_FOUND 예외가 발생한다") {
                    val exception = shouldThrow<GsmcException> { c.service.execute(alertId) }
                    exception.errorCode shouldBe ErrorCode.ALERT_NOT_FOUND
                    verify(exactly = 1) { c.alertRepo.findById(alertId) }
                    verify(exactly = 0) { c.alertRepo.deleteById(any()) }
                }
            }
        }

        Given("다른 사용자의 알림을 삭제하려 하면") {
            val c = ctx()
            val member = currentMember()
            val alertId = 11L
            val alertInstance = alert(receiver = anotherMember())

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findById(alertId) } returns alertInstance

            When("execute를 호출하면") {
                Then("AUTHENTICATION_FAILED 예외가 발생하고 삭제되지 않는다") {
                    val exception = shouldThrow<GsmcException> { c.service.execute(alertId) }
                    exception.errorCode shouldBe ErrorCode.AUTHENTICATION_FAILED
                    verify(exactly = 1) { c.alertRepo.findById(alertId) }
                    verify(exactly = 0) { c.alertRepo.deleteById(any()) }
                }
            }
        }
    })
