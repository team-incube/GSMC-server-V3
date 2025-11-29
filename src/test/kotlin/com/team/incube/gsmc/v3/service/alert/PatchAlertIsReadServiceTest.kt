package com.team.incube.gsmc.v3.service.alert
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.impl.PatchAlertIsReadServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class PatchAlertIsReadServiceTest :
    BehaviorSpec({
        data class TestData(
            val alertRepo: AlertExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: PatchAlertIsReadServiceImpl,
        )

        fun ctx(): TestData {
            val alertRepo = mockk<AlertExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val service = PatchAlertIsReadServiceImpl(alertRepo, currentMemberProvider)
            return TestData(alertRepo, currentMemberProvider, service)
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
        Given("특정 알림까지 읽음 처리할 때") {
            val c = ctx()
            val member =
                Member(id = 1L, name = "학생", email = "student@test.com", grade = 1, classNumber = 1, number = 1, role = MemberRole.STUDENT)
            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.updateIsReadTrueByReceiverIdAndLastAlertId(1L, 5L) } returns 1
            When("execute를 호출하면") {
                c.service.execute(5L)
                Then("해당 알림까지 읽음으로 업데이트된다") {
                    verify(exactly = 1) {
                        c.alertRepo.updateIsReadTrueByReceiverIdAndLastAlertId(receiverId = 1L, lastAlertId = 5L)
                    }
                }
            }
        }
    })
