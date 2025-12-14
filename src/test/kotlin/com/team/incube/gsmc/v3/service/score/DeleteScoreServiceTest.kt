package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.category.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.DeleteScoreServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.invoke
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.context.ApplicationEventPublisher

class DeleteScoreServiceTest :
    BehaviorSpec({
        data class TestContext(
            val scoreRepo: ScoreExposedRepository,
            val evidenceRepo: EvidenceExposedRepository,
            val fileRepo: FileExposedRepository,
            val alertRepo: AlertExposedRepository,
            val eventPublisher: ApplicationEventPublisher,
            val service: DeleteScoreServiceImpl,
        )

        fun ctx(): TestContext {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val evidenceRepo = mockk<EvidenceExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val alertRepo = mockk<AlertExposedRepository>()
            val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

            val service = DeleteScoreServiceImpl(scoreRepo, evidenceRepo, fileRepo, alertRepo, eventPublisher)
            return TestContext(scoreRepo, evidenceRepo, fileRepo, alertRepo, eventPublisher, service)
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

        fun baseScore(
            evidenceType: EvidenceType,
            sourceId: Long? = null,
        ): Score =
            Score(
                id = 1L,
                member = mockk(relaxed = true),
                categoryType = mockk { every { this@mockk.evidenceType } returns evidenceType },
                status = mockk(relaxed = true),
                sourceId = sourceId,
                activityName = null,
                scoreValue = null,
                rejectionReason = null,
            )

        Given("점수가 존재하지 않을 때") {
            val c = ctx()
            every { c.scoreRepo.findById(1L) } returns null

            Then("SCORE_NOT_FOUND 예외를 던진다") {
                val ex = shouldThrow<GsmcException> { c.service.execute(1L) }
                ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
            }
        }

        Given("증빙 타입이 EVIDENCE 인 점수를 삭제할 때") {
            val c = ctx()
            val files =
                listOf(
                    mockk<com.team.incube.gsmc.v3.domain.file.dto.File> {
                        every { id } returns 10L
                        every { uri } returns
                            "s3://f1"
                    },
                )
            val evidence =
                mockk<com.team.incube.gsmc.v3.domain.evidence.dto.Evidence> {
                    every { id } returns 5L
                    every { this@mockk.files } returns files
                }
            every { c.scoreRepo.findById(1L) } returns baseScore(EvidenceType.EVIDENCE, 5L)
            every { c.evidenceRepo.findById(5L) } returns evidence
            justRun { c.evidenceRepo.deleteById(5L) }
            justRun { c.fileRepo.deleteAllByIdIn(listOf(10L)) }
            every { c.alertRepo.deleteByScoreId(1L) } returns 1
            justRun { c.scoreRepo.deleteById(1L) }

            When("execute") { c.service.execute(1L) }

            Then("증빙 및 파일이 삭제되고 이벤트가 발생한다") {
                verify { c.alertRepo.deleteByScoreId(1L) }
                verify { c.fileRepo.deleteAllByIdIn(listOf(10L)) }
                verify { c.evidenceRepo.deleteById(5L) }
                verify { c.scoreRepo.deleteById(1L) }
                verify { c.eventPublisher.publishEvent(any<Any>()) }
            }
        }

        Given("증빙 타입이 FILE 인 점수를 삭제할 때") {
            val c = ctx()
            val file =
                mockk<com.team.incube.gsmc.v3.domain.file.dto.File> {
                    every { id } returns 7L
                    every { uri } returns "s3://f2"
                }
            every { c.scoreRepo.findById(2L) } returns baseScore(EvidenceType.FILE, 7L)
            every { c.fileRepo.findById(7L) } returns file
            justRun { c.fileRepo.deleteById(7L) }
            every { c.alertRepo.deleteByScoreId(2L) } returns 1
            justRun { c.scoreRepo.deleteById(2L) }

            When("execute") { c.service.execute(2L) }

            Then("파일이 삭제되고 이벤트가 발생한다") {
                verify { c.fileRepo.deleteById(7L) }
                verify { c.eventPublisher.publishEvent(any<Any>()) }
            }
        }

        Given("증빙이 필요 없는 점수를 삭제할 때") {
            val c = ctx()
            every { c.scoreRepo.findById(3L) } returns baseScore(EvidenceType.UNREQUIRED, null)
            every { c.alertRepo.deleteByScoreId(3L) } returns 1
            justRun { c.scoreRepo.deleteById(3L) }

            When("execute") { c.service.execute(3L) }

            Then("단순히 점수만 삭제한다") {
                verify(exactly = 0) { c.fileRepo.deleteAllByIdIn(any()) }
                verify { c.scoreRepo.deleteById(3L) }
            }
        }
    })
