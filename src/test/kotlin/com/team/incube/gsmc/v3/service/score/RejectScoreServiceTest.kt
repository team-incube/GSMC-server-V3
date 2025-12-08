package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.RejectScoreServiceImpl
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

class RejectScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val service: RejectScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val service = RejectScoreServiceImpl(scoreRepo)
            return TestData(scoreRepo, service)
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

        Given("유효한 scoreId와 rejectionReason으로 점수 거부에 성공할 때") {
            val c = ctx()
            val scoreId = 1L
            val rejectionReason = "증빙이 불충분합니다."

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

            every {
                c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                    scoreId = scoreId,
                    status = ScoreStatus.REJECTED,
                    rejectionReason = rejectionReason,
                )
            } returns 0

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreId, rejectionReason) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }
    })
