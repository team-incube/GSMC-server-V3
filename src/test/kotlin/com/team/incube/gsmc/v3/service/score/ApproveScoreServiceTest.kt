package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.ApproveScoreServiceImpl
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

class ApproveScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val memberRepo: com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository,
            val eventPublisher: ApplicationEventPublisher,
            val currentMemberProvider: CurrentMemberProvider,
            val service: ApproveScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val memberRepo = mockk<com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository>()
            val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns Member(1L, "Teacher", "t@test.com", 0, 0, 0, MemberRole.TEACHER)
            val service = ApproveScoreServiceImpl(scoreRepo, memberRepo, eventPublisher, currentMemberProvider)
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

        afterSpec { unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt") }

        Given("존재하는 점수를 승인할 때") {
            val c = ctx()
            val scoreId = 100L
            val score = mockk<Score>()
            every { score.member } returns Member(2L, "Student", "s@test.com", 1, 1, 1, MemberRole.STUDENT)
            every { score.id } returns scoreId
            every { c.scoreRepo.findById(scoreId) } returns score
            every {
                c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                    scoreId = scoreId,
                    status = ScoreStatus.APPROVED,
                    rejectionReason = null,
                )
            } returns 1

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("점수 상태가 업데이트되고 알림이 발행된다") {
                    verify(exactly = 1) {
                        c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                            scoreId = scoreId,
                            status = ScoreStatus.APPROVED,
                            rejectionReason = null,
                        )
                    }
                    verify(exactly = 1) { c.eventPublisher.publishEvent(any<Any>()) }
                }
            }
        }

        Given("존재하지 않는 점수를 승인할 때") {
            val c = ctx()
            val scoreId = 999L
            every { c.scoreRepo.findById(scoreId) } returns null

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreId) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }
    })
