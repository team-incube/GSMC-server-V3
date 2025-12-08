package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.ApproveScoreServiceImpl
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

class ApproveScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val service: ApproveScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val service = ApproveScoreServiceImpl(scoreRepo)
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

        Given("존재하는 점수를 승인할 때") {
            val c = ctx()
            val scoreId = 100L

            every {
                c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                    scoreId = scoreId,
                    status = ScoreStatus.APPROVED,
                    rejectionReason = null,
                )
            } returns 1

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("점수 상태가 업데이트된다") {
                    verify(exactly = 1) {
                        c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                            scoreId = scoreId,
                            status = ScoreStatus.APPROVED,
                            rejectionReason = null,
                        )
                    }
                }
            }
        }

        Given("존재하지 않는 점수를 승인할 때") {
            val c = ctx()
            val scoreId = 999L

            every {
                c.scoreRepo.updateStatusAndRejectionReasonByScoreId(
                    scoreId = scoreId,
                    status = ScoreStatus.APPROVED,
                    rejectionReason = null,
                )
            } returns 0

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreId) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }
    })
