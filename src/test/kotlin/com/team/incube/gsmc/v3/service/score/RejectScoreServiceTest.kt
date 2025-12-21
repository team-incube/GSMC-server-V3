package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.RejectScoreServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.context.ApplicationEventPublisher

class RejectScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val memberRepo: com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository,
            val eventPublisher: ApplicationEventPublisher,
            val currentMemberProvider: CurrentMemberProvider,
            val service: RejectScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val memberRepo = mockk<com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository>()
            val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            every { currentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 1L,
                    name = "Teacher",
                    email = "teacher@test.com",
                    grade = 0,
                    classNumber = 0,
                    number = 0,
                    role = MemberRole.TEACHER,
                )

            val service = RejectScoreServiceImpl(scoreRepo, memberRepo, eventPublisher, currentMemberProvider)
            return TestData(scoreRepo, memberRepo, eventPublisher, currentMemberProvider, service)
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

        Given("유효한 scoreId와 rejectionReason으로 점수 거부에 성공할 때") {
            val c = ctx()
            val scoreId = 1L
            val rejectionReason = "증빙이 불충분합니다."
            val member = Member(2L, "Student", "student@test.com", 1, 1, 1, MemberRole.STUDENT)
            val score = mockk<com.team.incube.gsmc.v3.domain.score.dto.Score>(relaxed = true)

            every { score.id } returns scoreId
            every { score.member } returns member
            every { c.scoreRepo.findById(scoreId) } returns score
            every {
                c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                    scoreId = scoreId,
                    status = ScoreStatus.REJECTED,
                    rejectionReason = rejectionReason,
                )
            } returns 1

            When("execute를 호출하면") {
                c.service.execute(scoreId, rejectionReason)

                Then("상태가 REJECTED로 업데이트되고 rejectionReason이 설정된다") {
                    verify(exactly = 1) {
                        c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                            scoreId = scoreId,
                            status = ScoreStatus.REJECTED,
                            rejectionReason = rejectionReason,
                        )
                    }
                }
            }
        }

        Given("존재하지 않는 scoreId로 거부를 시도할 때") {
            val c = ctx()
            val scoreId = 999L
            val rejectionReason = "증빙이 불충분합니다."

            every { c.scoreRepo.findById(scoreId) } returns null

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreId, rejectionReason) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }
    })
