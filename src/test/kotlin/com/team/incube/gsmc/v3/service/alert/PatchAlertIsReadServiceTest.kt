package com.team.incube.gsmc.v3.service.alert

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.impl.PatchAlertIsReadServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class PatchAlertIsReadServiceTest :
    BehaviorSpec({
        data class TestContext(
            val alertRepo: AlertExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: PatchAlertIsReadServiceImpl,
        )

        fun ctx(): TestContext {
            val alertRepo = mockk<AlertExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val service = PatchAlertIsReadServiceImpl(alertRepo, currentMemberProvider)
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

        fun member() =
            Member(
                id = 1L,
                name = "학생",
                email = "student@test.com",
                grade = 1,
                classNumber = 1,
                number = 1,
                role = MemberRole.STUDENT,
            )

        Given("특정 알림까지 읽음 처리할 때") {
            val c = ctx()
            val member = member()
            val lastAlertId = 5L

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every {
                c.alertRepo.updateIsReadTrueByReceiverIdAndLastAlertId(
                    receiverId = member.id,
                    lastAlertId = lastAlertId,
                )
            } returns 1

            When("execute를 호출하면") {
                c.service.execute(lastAlertId)

                Then("해당 알림까지 읽음으로 업데이트된다") {
                    verify(exactly = 1) {
                        c.alertRepo.updateIsReadTrueByReceiverIdAndLastAlertId(
                            receiverId = member.id,
                            lastAlertId = lastAlertId,
                        )
                    }
                }
            }
        }

        Given("다른 알림 ID로 읽음 처리할 때") {
            val c = ctx()
            val member = member()
            val lastAlertId = 10L

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every {
                c.alertRepo.updateIsReadTrueByReceiverIdAndLastAlertId(
                    receiverId = member.id,
                    lastAlertId = lastAlertId,
                )
            } returns 1

            When("execute를 호출하면") {
                c.service.execute(lastAlertId)

                Then("해당 알림 ID까지 읽음으로 업데이트된다") {
                    verify(exactly = 1) {
                        c.alertRepo.updateIsReadTrueByReceiverIdAndLastAlertId(
                            receiverId = member.id,
                            lastAlertId = lastAlertId,
                        )
                    }
                }
            }
        }
    })
