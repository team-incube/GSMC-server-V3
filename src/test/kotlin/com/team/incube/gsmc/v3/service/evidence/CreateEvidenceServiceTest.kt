package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.CreateEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.impl.CreateEvidenceServiceImpl
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

class CreateEvidenceServiceTest :
    BehaviorSpec({
        data class TestData(
            val evidenceRepo: EvidenceExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val fileRepo: FileExposedRepository,
            val service: CreateEvidenceServiceImpl,
        )

        fun ctx(): TestData {
            val evidenceRepo = mockk<EvidenceExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val service = CreateEvidenceServiceImpl(evidenceRepo, scoreRepo, fileRepo)
            return TestData(evidenceRepo, scoreRepo, fileRepo, service)
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

        Given("유효한 scoreIds와 fileIds로 증빙 생성에 성공할 때") {
            val c = ctx()
            val now = LocalDateTime.of(2025, 10, 1, 12, 0, 0)
            val scoreIds = listOf(1L, 2L)
            val fileIds = listOf(10L, 11L)
            val files =
                listOf(
                    File(
                        fileId = 10L,
                        memberId = 0L,
                        fileOriginalName = "a.pdf",
                        fileStoredName = "s-a.pdf",
                        fileUri = "uri-a",
                    ),
                    File(
                        fileId = 11L,
                        memberId = 0L,
                        fileOriginalName = "b.jpg",
                        fileStoredName = "s-b.jpg",
                        fileUri = "uri-b",
                    ),
                )
            val saved =
                Evidence(
                    id = 100L,
                    memberId = 0L,
                    title = "title",
                    content = "content",
                    createdAt = now,
                    updatedAt = now,
                    files = files,
                )

            every { c.scoreRepo.existsByIdIn(scoreIds) } returns true
            every { c.scoreRepo.existsAnyWithSource(scoreIds) } returns false
            every { c.fileRepo.existsByIdIn(fileIds) } returns true
            every { c.evidenceRepo.save(userId = 0L, title = "title", content = "content", fileIds = fileIds) } returns
                saved
            justRun { c.scoreRepo.updateSourceId(scoreIds, saved.id) }

            When("execute를 호출하면") {
                val res: CreateEvidenceResponse = c.service.execute(scoreIds, "title", "content", fileIds)

                Then("정상적으로 생성되어 응답이 반환된다") {
                    res shouldNotBe null
                    res.id shouldBe 100L
                    res.title shouldBe "title"
                    res.content shouldBe "content"
                    res.createAt shouldBe now
                    res.updateAt shouldBe now
                    res.file.size shouldBe 2
                }

                Then("점수, 파일 검증과 저장 및 score source 업데이트가 호출된다") {
                    verify(exactly = 1) { c.scoreRepo.existsByIdIn(scoreIds) }
                    verify(exactly = 1) { c.scoreRepo.existsAnyWithSource(scoreIds) }
                    verify(exactly = 1) { c.fileRepo.existsByIdIn(fileIds) }
                    verify(
                        exactly = 1,
                    ) { c.evidenceRepo.save(userId = 0L, title = "title", content = "content", fileIds = fileIds) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceId(scoreIds, saved.id) }
                }
            }
        }

        Given("scoreIds 중 존재하지 않는 점수가 포함될 때") {
            val c = ctx()
            val scoreIds = listOf(1L, 2L)
            val fileIds = listOf(10L)
            every { c.scoreRepo.existsByIdIn(scoreIds) } returns false

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreIds, "t", "c", fileIds) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }

        Given("이미 증빙이 연결된 score가 있을 때") {
            val c = ctx()
            val scoreIds = listOf(1L)
            val fileIds = listOf(10L)
            every { c.scoreRepo.existsByIdIn(scoreIds) } returns true
            every { c.scoreRepo.existsAnyWithSource(scoreIds) } returns true

            When("execute를 호출하면") {
                Then("SCORE_ALREADY_HAS_EVIDENCE 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreIds, "t", "c", fileIds) }
                    ex.errorCode shouldBe ErrorCode.SCORE_ALREADY_HAS_EVIDENCE
                }
            }
        }

        Given("fileIds가 주어졌지만 존재하지 않을 때") {
            val c = ctx()
            val scoreIds = listOf(1L)
            val fileIds = listOf(999L)
            every { c.scoreRepo.existsByIdIn(scoreIds) } returns true
            every { c.scoreRepo.existsAnyWithSource(scoreIds) } returns false
            every { c.fileRepo.existsByIdIn(fileIds) } returns false

            When("execute를 호출하면") {
                Then("FILE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreIds, "t", "c", fileIds) }
                    ex.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }
        }

        Given("fileIds가 비어있을 때") {
            val c = ctx()
            val now = LocalDateTime.of(2025, 10, 1, 12, 0, 0)
            val scoreIds = listOf(1L, 2L)
            val fileIds = emptyList<Long>()
            val saved =
                Evidence(
                    id = 101L,
                    memberId = 0L,
                    title = "t",
                    content = "c",
                    createdAt = now,
                    updatedAt = now,
                    files = emptyList(),
                )

            every { c.scoreRepo.existsByIdIn(scoreIds) } returns true
            every { c.scoreRepo.existsAnyWithSource(scoreIds) } returns false
            every { c.evidenceRepo.save(userId = 0L, title = "t", content = "c", fileIds = fileIds) } returns saved
            justRun { c.scoreRepo.updateSourceId(scoreIds, saved.id) }

            When("execute를 호출하면") {
                val res = c.service.execute(scoreIds, "t", "c", fileIds)

                Then("정상적으로 생성되고 파일 검증은 호출되지 않는다") {
                    res.id shouldBe 101L
                    verify(exactly = 0) { c.fileRepo.existsByIdIn(any()) }
                    verify(
                        exactly = 1,
                    ) { c.evidenceRepo.save(userId = 0L, title = "t", content = "c", fileIds = fileIds) }
                    verify(exactly = 1) { c.scoreRepo.updateSourceId(scoreIds, saved.id) }
                }
            }
        }
    })
