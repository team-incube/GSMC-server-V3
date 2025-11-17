package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.PatchEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.impl.UpdateEvidenceServiceImpl
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class UpdateEvidenceServiceTest :
    BehaviorSpec({
        data class Ctx(
            val evidenceRepo: EvidenceExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val fileRepo: FileExposedRepository,
            val service: UpdateEvidenceServiceImpl,
        )

        fun ctx(): Ctx {
            val e = mockk<EvidenceExposedRepository>()
            val s = mockk<ScoreExposedRepository>()
            val f = mockk<FileExposedRepository>()
            val svc = UpdateEvidenceServiceImpl(e, s, f)
            return Ctx(e, s, f, svc)
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

        Given("증빙이 존재하고 참여자와 파일이 모두 변경되는 경우") {
            val c = ctx()
            val id = 1L
            val userId = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val originFiles = listOf(File(10, userId, "a.pdf", "sa.pdf", "uri-a"))
            val found = Evidence(id, userId, "old-title", "old-content", now, now, originFiles)
            val newParticipantId = 100L
            val newFileIds = listOf(20L, 21L)
            val updatedFiles =
                listOf(
                    File(20, userId, "b.pdf", "sb.pdf", "uri-b"),
                    File(21, userId, "c.jpg", "sc.jpg", "uri-c"),
                )
            val updated = Evidence(id, userId, "new-title", "new-content", now, now, updatedFiles)

            every { c.evidenceRepo.findById(id) } returns found
            every { c.fileRepo.existsByIdIn(newFileIds) } returns true
            every { c.scoreRepo.existsById(newParticipantId) } returns true
            justRun { c.scoreRepo.updateSourceIdToNull(id) }
            justRun { c.scoreRepo.updateSourceId(newParticipantId, id) }
            every {
                c.evidenceRepo.update(id = id, title = "new-title", content = "new-content", fileIds = newFileIds)
            } returns updated

            When("execute를 호출하면") {
                val res: PatchEvidenceResponse =
                    c.service.execute(id, newParticipantId, "new-title", "new-content", newFileIds)

                Then("수정된 정보가 반환된다") {
                    res shouldNotBe null
                    res.id shouldBe id
                    res.title shouldBe "new-title"
                    res.content shouldBe "new-content"
                    res.file shouldBe updatedFiles
                }

                Then("참여자 재매핑 및 파일 검증/업데이트가 수행된다") {
                    verify(exactly = 1) { c.fileRepo.existsByIdIn(newFileIds) }
                    verify(exactly = 1) { c.scoreRepo.existsById(newParticipantId) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceIdToNull(id) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceId(newParticipantId, id) }
                    verify(exactly = 1) {
                        c.evidenceRepo.update(
                            id = id,
                            title = "new-title",
                            content = "new-content",
                            fileIds = newFileIds,
                        )
                    }
                }
            }
        }

        Given("증빙이 존재하지 않을 때") {
            val c = ctx()
            val id = 404L
            every { c.evidenceRepo.findById(id) } returns null

            When("execute를 호출하면") {
                Then("EVIDENCE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(id, null, null, null, null) }
                    ex.errorCode shouldBe ErrorCode.EVIDENCE_NOT_FOUND
                }
            }
        }

        Given("파일 IDs가 주어졌지만 존재하지 않을 때") {
            val c = ctx()
            val id = 1L
            val userId = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val found = Evidence(id, userId, "t", "c", now, now, emptyList())
            every { c.evidenceRepo.findById(id) } returns found
            every { c.fileRepo.existsByIdIn(listOf(999L)) } returns false

            When("execute를 호출하면") {
                Then("FILE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(id, null, null, null, listOf(999L)) }
                    ex.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }
        }

        Given("참여자 ID가 주어졌지만 존재하지 않을 때") {
            val c = ctx()
            val id = 1L
            val userId = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val found = Evidence(id, userId, "t", "c", now, now, emptyList())
            every { c.evidenceRepo.findById(id) } returns found
            every { c.fileRepo.existsByIdIn(any()) } returns true
            every { c.scoreRepo.existsById(100L) } returns false

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(
                                id,
                                100L,
                                "nt",
                                "nc",
                                listOf(10L),
                            )
                        }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                    verify(exactly = 0) { c.scoreRepo.updateSourceIdToNull(any()) }
                    verify(exactly = 0) { c.scoreRepo.updateSourceId(any(), any()) }
                }
            }
        }

        Given("타이틀/내용/파일/참여자 모두 null이면 기존 값으로 업데이트 된다") {
            val c = ctx()
            val id = 1L
            val userId = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val originFiles = listOf(File(10, userId, "a.pdf", "sa.pdf", "uri-a"))
            val found = Evidence(id, userId, "t0", "c0", now, now, originFiles)
            val updated = Evidence(id, userId, "t0", "c0", now, now, originFiles)

            every { c.evidenceRepo.findById(id) } returns found
            every {
                c.evidenceRepo.update(id = id, title = "t0", content = "c0", fileIds = listOf(10L))
            } returns updated

            When("execute를 호출하면") {
                val res = c.service.execute(id, null, null, null, null)

                Then("원본 값으로 업데이트 호출되고 반환된다") {
                    res.id shouldBe id
                    verify(
                        exactly = 1,
                    ) { c.evidenceRepo.update(id = id, title = "t0", content = "c0", fileIds = listOf(10L)) }
                    verify(exactly = 0) { c.fileRepo.existsByIdIn(any()) }
                    verify(exactly = 0) { c.scoreRepo.updateSourceIdToNull(any()) }
                    verify(exactly = 0) { c.scoreRepo.updateSourceId(any(), any()) }
                }
            }
        }

        Given("제목만 변경하고 나머지는 유지하는 경우") {
            val c = ctx()
            val id = 2L
            val userId = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val originFiles = listOf(File(10, userId, "a.pdf", "sa.pdf", "uri-a"))
            val found = Evidence(id, userId, "old", "keep", now, now, originFiles)
            val updated = Evidence(id, userId, "new", "keep", now, now, originFiles)
            every { c.evidenceRepo.findById(id) } returns found
            every { c.evidenceRepo.update(id = id, title = "new", content = "keep", fileIds = listOf(10L)) } returns
                updated

            When("execute를 호출하면") {
                val res = c.service.execute(id, null, "new", null, null)

                Then("제목만 변경되어 반환된다") {
                    res.title shouldBe "new"
                    res.content shouldBe "keep"
                    res.file shouldBe originFiles
                }
            }
        }
    })
