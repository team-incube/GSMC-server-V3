package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.impl.DeleteEvidenceServiceImpl
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.s3.S3BulkFileDeletionEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class DeleteEvidenceServiceTest :
    BehaviorSpec({
        data class Ctx(
            val evidenceRepo: EvidenceExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val fileRepo: FileExposedRepository,
            val eventPublisher: ApplicationEventPublisher,
            val service: DeleteEvidenceServiceImpl,
        )

        fun ctx(): Ctx {
            val e = mockk<EvidenceExposedRepository>()
            val s = mockk<ScoreExposedRepository>()
            val f = mockk<FileExposedRepository>()
            val p = mockk<ApplicationEventPublisher>()
            val svc = DeleteEvidenceServiceImpl(e, s, f, p)
            return Ctx(e, s, f, p, svc)
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

        Given("파일이 있는 증빙을 삭제하면") {
            val c = ctx()
            val id = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val file = File(id = 1, member = 0L, originalName = "a", storeName = "sa", uri = "s3://bucket/a")
            val evidence =
                Evidence(
                    id,
                    member = 0L,
                    title = "t",
                    content = "c",
                    createdAt = now,
                    updatedAt = now,
                    files = listOf(file),
                )
            every { c.evidenceRepo.findById(id) } returns evidence
            justRun { c.fileRepo.deleteAllByIdIn(listOf(file.id)) }
            justRun { c.eventPublisher.publishEvent(any<S3BulkFileDeletionEvent>()) }
            justRun { c.scoreRepo.updateSourceIdToNull(id) }
            justRun { c.evidenceRepo.deleteById(id) }

            When("execute를 호출하면") {
                c.service.execute(id)

                Then("파일 삭제, S3 이벤트 발행, source null 처리, 증빙 삭제가 각각 1회 호출되고 이벤트에 올바른 URI가 포함된다") {
                    verify(exactly = 1) { c.evidenceRepo.findById(id) }
                    verify(exactly = 1) { c.fileRepo.deleteAllByIdIn(listOf(file.id)) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceIdToNull(id) }
                    verify(exactly = 1) { c.evidenceRepo.deleteById(id) }

                    val eventSlot = slot<S3BulkFileDeletionEvent>()
                    verify(exactly = 1) { c.eventPublisher.publishEvent(capture(eventSlot)) }
                    eventSlot.captured.fileUris shouldBe listOf(file.uri)
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

        Given("파일이 없는 증빙을 삭제하면") {
            val c = ctx()
            val id = 2L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val evidence =
                Evidence(
                    id,
                    member = 0L,
                    title = "no files",
                    content = "content",
                    createdAt = now,
                    updatedAt = now,
                    files = emptyList(),
                )
            every { c.evidenceRepo.findById(id) } returns evidence
            justRun { c.scoreRepo.updateSourceIdToNull(id) }
            justRun { c.evidenceRepo.deleteById(id) }

            When("execute를 호출하면") {
                c.service.execute(id)

                Then("파일 삭제와 이벤트 발행은 호출되지 않고, source null 처리와 증빙 삭제만 호출된다") {
                    verify(exactly = 1) { c.evidenceRepo.findById(id) }
                    verify(exactly = 0) { c.fileRepo.deleteAllByIdIn(any()) }
                    verify(exactly = 0) { c.eventPublisher.publishEvent(any<S3BulkFileDeletionEvent>()) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceIdToNull(id) }
                    verify(exactly = 1) { c.evidenceRepo.deleteById(id) }
                }
            }
        }

        Given("여러 파일이 있는 증빙을 삭제하면") {
            val c = ctx()
            val id = 3L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val files =
                listOf(
                    File(id = 10, member = 0L, originalName = "file1.pdf", storeName = "s1", uri = "s3://bucket/file1"),
                    File(id = 11, member = 0L, originalName = "file2.jpg", storeName = "s2", uri = "s3://bucket/file2"),
                    File(id = 12, member = 0L, originalName = "file3.docx", storeName = "s3", uri = "s3://bucket/file3"),
                )
            val evidence =
                Evidence(
                    id,
                    member = 0L,
                    title = "multiple files",
                    content = "content",
                    createdAt = now,
                    updatedAt = now,
                    files = files,
                )
            every { c.evidenceRepo.findById(id) } returns evidence
            justRun { c.fileRepo.deleteAllByIdIn(files.map { it.id }) }
            justRun { c.eventPublisher.publishEvent(any<S3BulkFileDeletionEvent>()) }
            justRun { c.scoreRepo.updateSourceIdToNull(id) }
            justRun { c.evidenceRepo.deleteById(id) }

            When("execute를 호출하면") {
                c.service.execute(id)

                Then("모든 파일이 삭제되고 모든 URI가 이벤트에 포함되며 전체 프로세스가 실행된다") {
                    verify(exactly = 1) { c.evidenceRepo.findById(id) }
                    verify(exactly = 1) { c.fileRepo.deleteAllByIdIn(files.map { it.id }) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceIdToNull(id) }
                    verify(exactly = 1) { c.evidenceRepo.deleteById(id) }

                    val eventSlot = slot<S3BulkFileDeletionEvent>()
                    verify(exactly = 1) { c.eventPublisher.publishEvent(capture(eventSlot)) }
                    eventSlot.captured.fileUris shouldBe files.map { it.uri }
                }
            }
        }
    })
