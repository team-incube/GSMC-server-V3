package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.impl.DeleteEvidenceServiceImpl
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class DeleteEvidenceServiceTest :
    BehaviorSpec({
        data class Ctx(
            val evidenceRepo: EvidenceExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val service: DeleteEvidenceServiceImpl,
        )

        fun ctx(): Ctx {
            val e = mockk<EvidenceExposedRepository>()
            val s = mockk<ScoreExposedRepository>()
            val svc = DeleteEvidenceServiceImpl(e, s)
            return Ctx(e, s, svc)
        }

        beforeTest {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                secondArg<Transaction.() -> Any>().invoke(mockk(relaxed = true))
            }
        }
        afterTest { unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt") }

        Given("존재하는 증빙 ID가 주어지면 삭제에 성공한다") {
            val c = ctx()
            val id = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val evidence = Evidence(id, 0L, "t", "c", now, now, listOf(File(1, 0L, "a", "sa", "uri")))
            every { c.evidenceRepo.findById(id) } returns evidence
            justRun { c.scoreRepo.updateSourceIdToNull(id) }
            justRun { c.evidenceRepo.deleteById(id) }

            When("execute를 호출하면") {
                c.service.execute(id)

                Then("조회, 점수 source null 처리, 증빙 삭제가 각각 1회 호출된다") {
                    verify(exactly = 1) { c.evidenceRepo.findById(id) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceIdToNull(id) }
                    verify(exactly = 1) { c.evidenceRepo.deleteById(id) }
                }
            }
        }

        Given("존재하지 않는 증빙 ID가 주어지면") {
            val c = ctx()
            val id = 999L
            every { c.evidenceRepo.findById(id) } returns null

            When("execute를 호출하면") {
                Then("EVIDENCE_NOT_FOUND 예외가 발생하고 이후 동작은 호출되지 않는다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(id) }
                    ex.errorCode shouldBe ErrorCode.EVIDENCE_NOT_FOUND
                    verify(exactly = 0) { c.scoreRepo.updateSourceIdToNull(any()) }
                    verify(exactly = 0) { c.evidenceRepo.deleteById(any()) }
                }
            }
        }

        Given("여러 증빙을 순차적으로 삭제하면 각각의 상호작용이 일어난다") {
            val c = ctx()
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val ids = listOf(1L, 2L, 3L)
            ids.forEach { i ->
                every { c.evidenceRepo.findById(i) } returns Evidence(i, 0L, "t$i", "c$i", now, now, emptyList())
                justRun { c.scoreRepo.updateSourceIdToNull(i) }
                justRun { c.evidenceRepo.deleteById(i) }
            }

            When("각각 삭제를 호출하면") {
                ids.forEach { c.service.execute(it) }

                Then("각 ID에 대해 find -> updateSourceIdToNull -> deleteById가 1회씩 호출된다") {
                    ids.forEach {
                        verify(exactly = 1) { c.evidenceRepo.findById(it) }
                        verify(exactly = 1) { c.scoreRepo.updateSourceIdToNull(it) }
                        verify(exactly = 1) { c.evidenceRepo.deleteById(it) }
                    }
                }
            }
        }
    })
