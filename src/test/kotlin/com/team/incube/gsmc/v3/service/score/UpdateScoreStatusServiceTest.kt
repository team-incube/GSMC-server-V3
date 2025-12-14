package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.UpdateScoreStatusServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class UpdateScoreStatusServiceTest :
    FunSpec({
        data class Ctx(
            val scoreRepo: ScoreExposedRepository,
            val service: UpdateScoreStatusServiceImpl,
        )

        fun ctx(): Ctx {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val service = UpdateScoreStatusServiceImpl(scoreRepo)
            return Ctx(scoreRepo, service)
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

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        test("유효한 scoreId와 status로 상태 업데이트에 성공한다") {
            val c = ctx()
            val scoreId = 1L
            val status = ScoreStatus.APPROVED
            every { c.scoreRepo.updateStatusByScoreId(scoreId, status) } returns 1

            c.service.execute(scoreId, status)

            verify(exactly = 1) { c.scoreRepo.updateStatusByScoreId(scoreId, status) }
        }

        test("존재하지 않는 scoreId로 상태 업데이트를 시도하면 SCORE_NOT_FOUND 예외가 발생한다") {
            val c = ctx()
            val scoreId = 999L
            val status = ScoreStatus.REJECTED
            every { c.scoreRepo.updateStatusByScoreId(scoreId, status) } returns 0

            val ex = shouldThrow<GsmcException> { c.service.execute(scoreId, status) }
            ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
            verify(exactly = 1) { c.scoreRepo.updateStatusByScoreId(scoreId, status) }
        }

        test("모든 상태 값이 정확히 전달되어 업데이트된다") {
            val scoreIdBase = 1000L
            ScoreStatus.entries.forEachIndexed { idx, status ->
                val c = ctx()
                val scoreId = scoreIdBase + idx
                every { c.scoreRepo.updateStatusByScoreId(scoreId, status) } returns 1

                c.service.execute(scoreId, status)

                verify(exactly = 1) { c.scoreRepo.updateStatusByScoreId(scoreId, status) }
            }
        }
    })
