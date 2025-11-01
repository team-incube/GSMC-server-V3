package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.UpdateScoreStatusServiceImpl
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

class UpdateScoreStatusServiceTest :
    BehaviorSpec({
        data class Ctx(
            val scoreRepo: ScoreExposedRepository,
            val service: UpdateScoreStatusServiceImpl,
        )

        fun ctx(): Ctx {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val service = UpdateScoreStatusServiceImpl(scoreRepo)
            return Ctx(scoreRepo, service)
        }

        beforeTest {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                val stmt = invocation.args[1] as Transaction.() -> Any
                stmt.invoke(mockk(relaxed = true))
            }
        }
        afterTest { unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt") }

        Given("유효한 scoreId와 status로 상태 업데이트에 성공할 때") {
            val c = ctx()
            val scoreId = 1L
            val status = ScoreStatus.APPROVED
            every { c.scoreRepo.updateStatusByScoreId(scoreId, status) } returns 1

            When("execute를 호출하면") {
                c.service.execute(scoreId, status)

                Then("리포지토리 업데이트가 1회 호출되고 예외가 발생하지 않는다") {
                    verify(exactly = 1) { c.scoreRepo.updateStatusByScoreId(scoreId, status) }
                }
            }
        }

        Given("존재하지 않는 scoreId로 상태 업데이트를 시도할 때") {
            val c = ctx()
            val scoreId = 999L
            val status = ScoreStatus.REJECTED
            every { c.scoreRepo.updateStatusByScoreId(scoreId, status) } returns 0

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreId, status) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                    verify(exactly = 1) { c.scoreRepo.updateStatusByScoreId(scoreId, status) }
                }
            }
        }

        Given("모든 상태 값이 정확히 전달되어 업데이트되는지 검증한다") {
            val scoreIdBase = 1000L
            ScoreStatus.entries.forEachIndexed { idx, status ->
                val c = ctx()
                val scoreId = scoreIdBase + idx
                every { c.scoreRepo.updateStatusByScoreId(scoreId, status) } returns 1

                When("status=$status 로 execute 호출") {
                    c.service.execute(scoreId, status)

                    Then("updateStatusByScoreId(scoreId, $status)가 정확히 1회 호출된다") {
                        verify(exactly = 1) { c.scoreRepo.updateStatusByScoreId(scoreId, status) }
                    }
                }
            }
        }
    })
