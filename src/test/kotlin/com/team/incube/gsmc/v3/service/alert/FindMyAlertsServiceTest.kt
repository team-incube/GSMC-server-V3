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
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class FindMyAlertsServiceTest :
    BehaviorSpec({
        data class TestData(
            val currentMemberProvider: CurrentMemberProvider,
            val alertRepo: AlertExposedRepository,
            val service: FindMyAlertsServiceImpl,
        )

        fun ctx(): TestData {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val alertRepo = mockk<AlertExposedRepository>()
            val service = FindMyAlertsServiceImpl(currentMemberProvider, alertRepo)
            return TestData(currentMemberProvider, alertRepo, service)
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
        Given("내 알림 목록을 조회할 때") {
            val c = ctx()
            val member =
                Member(id = 1L, name = "학생", email = "student@test.com", grade = 1, classNumber = 1, number = 1, role = MemberRole.STUDENT)
            val teacher =
                Member(id = 2L, name = "선생님", email = "teacher@test.com", grade = 0, classNumber = 0, number = 0, role = MemberRole.TEACHER)
            val score =
                Score(
                    id = 1L,
                    member = member,
                    categoryType = CategoryType.CERTIFICATE,
                    status = ScoreStatus.APPROVED,
                    sourceId = null,
                    activityName = "정보처리기능사",
                    scoreValue = 5.0,
                    rejectionReason = null,
                )
            val alerts =
                listOf(
                    Alert(
                        id = 1L,
                        sender = teacher,
                        receiver = member,
                        score = score,
                        alertType = AlertType.APPROVED,
                        isRead = false,
                        content = "정보처리기능사 점수를 선생님 선생님께서 통과시키셨습니다.",
                        createdAt = LocalDateTime.now(),
                    ),
                )
            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findAllByReceiverId(1L) } returns alerts
            When("execute를 호출하면") {
                val result = c.service.execute()
                Then("알림 목록이 반환된다") {
                    result.alerts.size shouldBe 1
                    result.alerts[0].id shouldBe 1L
                    result.alerts[0].alertType shouldBe AlertType.APPROVED
                    result.alerts[0].scoreId shouldBe 1L
                }
            }
        }
        Given("알림이 없는 사용자가 조회할 때") {
            val c = ctx()
            val member =
                Member(id = 1L, name = "학생", email = "student@test.com", grade = 1, classNumber = 1, number = 1, role = MemberRole.STUDENT)
            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.alertRepo.findAllByReceiverId(1L) } returns emptyList()
            When("execute를 호출하면") {
                val result = c.service.execute()
                Then("빈 목록이 반환된다") {
                    result.alerts.size shouldBe 0
                }
            }
        }
    })
