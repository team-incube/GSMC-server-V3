package com.team.incube.gsmc.v3.service.alert

import com.team.incube.gsmc.v3.domain.alert.dto.Alert
import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.impl.FindMyAlertsServiceImpl
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import java.time.LocalDateTime

class FindMyAlertsServiceTest :
    BehaviorSpec({
        data class TestContext(
            val currentMemberProvider: CurrentMemberProvider,
            val alertRepo: AlertExposedRepository,
            val service: FindMyAlertsServiceImpl,
        )

        fun ctx(): TestContext {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val alertRepo = mockk<AlertExposedRepository>()
            val service = FindMyAlertsServiceImpl(currentMemberProvider, alertRepo)
            return TestContext(currentMemberProvider, alertRepo, service)
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

        fun teacher() =
            Member(
                id = 2L,
                name = "선생님",
                email = "teacher@test.com",
                grade = 0,
                classNumber = 0,
                number = 0,
                role = MemberRole.TEACHER,
            )

        fun score(owner: Member = member()) =
            Score(
                id = 1L,
                member = owner,
                categoryType = CategoryType.CERTIFICATE,
                status = ScoreStatus.APPROVED,
                sourceId = null,
                activityName = "정보처리기능사",
                scoreValue = 5.0,
                rejectionReason = null,
            )

        Given("내 알림 목록을 조회할 때") {
            val c = ctx()
            val member = member()
            val teacherMember = teacher()
            val scoreInstance = score(owner = member)
            val alerts =
                listOf(
                    Alert(
                        id = 1L,
                        sender = teacherMember,
                        receiver = member,
                        score = scoreInstance,
                        alertType = AlertType.APPROVED,
                        isRead = false,
                        content = "정보처리기능사 점수를 선생님 선생님께서 통과시키셨습니다.",
                        createdAt = LocalDateTime.now(),
                    ),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findAllByReceiverId(member.id) } returns alerts

            When("execute를 호출하면") {
                val result = c.service.execute()

                Then("알림 목록이 반환된다") {
                    result.alerts.size shouldBe 1
                    result.alerts[0].id shouldBe 1L
                    result.alerts[0].alertType shouldBe AlertType.APPROVED
                    result.alerts[0].scoreId shouldBe 1L
                    result.alerts[0].title shouldBe AlertType.APPROVED.title
                    result.alerts[0].content shouldBe "정보처리기능사 점수를 선생님 선생님께서 통과시키셨습니다."
                }
            }
        }

        Given("알림이 없는 사용자가 조회할 때") {
            val c = ctx()
            val member = member()

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findAllByReceiverId(member.id) } returns emptyList()

            When("execute를 호출하면") {
                val result = c.service.execute()

                Then("빈 목록이 반환된다") {
                    result.alerts.size shouldBe 0
                }
            }
        }

        Given("여러 개의 알림이 있을 때") {
            val c = ctx()
            val member = member()
            val teacherMember = teacher()
            val scoreInstance = score(owner = member)
            val alerts =
                listOf(
                    Alert(
                        id = 1L,
                        sender = teacherMember,
                        receiver = member,
                        score = scoreInstance,
                        alertType = AlertType.APPROVED,
                        isRead = false,
                        content = "알림 1",
                        createdAt = LocalDateTime.now(),
                    ),
                    Alert(
                        id = 2L,
                        sender = teacherMember,
                        receiver = member,
                        score = scoreInstance,
                        alertType = AlertType.REJECTED,
                        isRead = true,
                        content = "알림 2",
                        createdAt = LocalDateTime.now(),
                    ),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findAllByReceiverId(member.id) } returns alerts

            When("execute를 호출하면") {
                val result = c.service.execute()

                Then("모든 알림이 반환된다") {
                    result.alerts.size shouldBe 2
                    result.alerts[0].id shouldBe 1L
                    result.alerts[0].alertType shouldBe AlertType.APPROVED
                    result.alerts[1].id shouldBe 2L
                    result.alerts[1].alertType shouldBe AlertType.REJECTED
                }
            }
        }
    })
