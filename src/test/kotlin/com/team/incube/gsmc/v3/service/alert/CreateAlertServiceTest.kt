package com.team.incube.gsmc.v3.service.alert
import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.impl.CreateAlertServiceImpl
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
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

class CreateAlertServiceTest :
    BehaviorSpec({
        data class TestData(
            val memberRepo: MemberExposedRepository,
            val alertRepo: AlertExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val service: CreateAlertServiceImpl,
        )

        fun ctx(): TestData {
            val memberRepo = mockk<MemberExposedRepository>()
            val alertRepo = mockk<AlertExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val service = CreateAlertServiceImpl(memberRepo, alertRepo, scoreRepo)
            return TestData(memberRepo, alertRepo, scoreRepo, service)
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
        Given("승인 알림을 생성할 때") {
            val c = ctx()
            val sender =
                Member(
                    id = 1L,
                    name = "선생님",
                    email = "teacher@test.com",
                    grade = 0,
                    classNumber = 0,
                    number = 0,
                    role = MemberRole.TEACHER,
                )
            val receiver =
                Member(
                    id = 2L,
                    name = "학생",
                    email = "student@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )
            val score =
                Score(
                    id = 1L,
                    member = receiver,
                    categoryType = CategoryType.CERTIFICATE,
                    status = ScoreStatus.PENDING,
                    sourceId = null,
                    activityName = "정보처리기능사",
                    scoreValue = 5.0,
                    rejectionReason = null,
                )
            every { c.memberRepo.findById(1L) } returns sender
            every { c.memberRepo.findById(2L) } returns receiver
            every { c.scoreRepo.findById(1L) } returns score
            every { c.alertRepo.save(any(), any(), any(), any(), any()) } returns mockk(relaxed = true)
            When("execute를 호출하면") {
                c.service.execute(senderId = 1L, receiverId = 2L, scoreId = 1L, alertType = AlertType.APPROVED)
                Then("올바른 내용의 알림이 저장된다") {
                    verify(exactly = 1) {
                        c.alertRepo.save(sender, receiver, score, AlertType.APPROVED, "정보처리기능사 점수를 선생님 선생님께서 통과시키셨습니다.")
                    }
                }
            }
        }
        Given("존재하지 않는 회원에게 알림을 보내려고 할 때") {
            val c = ctx()
            val sender =
                Member(id = 1L, name = "선생님", email = "teacher@test.com", grade = 0, classNumber = 0, number = 0, role = MemberRole.TEACHER)
            every { c.memberRepo.findById(1L) } returns sender
            every { c.memberRepo.findById(999L) } returns null
            When("execute를 호출하면") {
                Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(
                                senderId = 1L,
                                receiverId = 999L,
                                scoreId = 1L,
                                alertType = AlertType.APPROVED,
                            )
                        }
                    ex.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                }
            }
        }
        Given("존재하지 않는 점수로 알림을 생성하려고 할 때") {
            val c = ctx()
            val sender =
                Member(id = 1L, name = "선생님", email = "teacher@test.com", grade = 0, classNumber = 0, number = 0, role = MemberRole.TEACHER)
            val receiver =
                Member(id = 2L, name = "학생", email = "student@test.com", grade = 1, classNumber = 1, number = 1, role = MemberRole.STUDENT)
            every { c.memberRepo.findById(1L) } returns sender
            every { c.memberRepo.findById(2L) } returns receiver
            every { c.scoreRepo.findById(999L) } returns null
            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(
                                senderId = 1L,
                                receiverId = 2L,
                                scoreId = 999L,
                                alertType = AlertType.APPROVED,
                            )
                        }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }
    })
