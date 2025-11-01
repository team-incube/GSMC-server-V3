package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.dto.Category
import com.team.incube.gsmc.v3.domain.category.dto.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.DeleteScoreServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
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

class DeleteScoreServiceTest :
    BehaviorSpec({
        data class Ctx(
            val scoreRepo: ScoreExposedRepository,
            val evidenceRepo: EvidenceExposedRepository,
            val fileRepo: FileExposedRepository,
            val s3: S3DeleteService,
            val service: DeleteScoreServiceImpl,
        )

        fun ctx(): Ctx {
            val s = mockk<ScoreExposedRepository>()
            val e = mockk<EvidenceExposedRepository>()
            val f = mockk<FileExposedRepository>()
            val s3 = mockk<S3DeleteService>()
            val svc = DeleteScoreServiceImpl(s, e, f, s3)
            return Ctx(s, e, f, s3, svc)
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

        fun member() =
            Member(
                id = 1L,
                name = "m",
                email = "m@g.com",
                grade = 1,
                classNumber = 1,
                number = 1,
                role = MemberRole.STUDENT,
            )

        fun category(type: EvidenceType) =
            Category(
                id = 1L,
                englishName = "cat",
                koreanName = "카테고리",
                weight = 1,
                maximumValue = 10,
                isAccumulated = false,
                evidenceType = type,
            )

        Given("증빙형(EVIDENCE) 점수를 삭제할 때 연결된 증빙과 파일, S3 객체도 삭제된다") {
            val c = ctx()
            val scoreId = 1L
            val sourceId = 100L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val files =
                listOf(
                    File(fileId = 10L, fileOriginalName = "a.pdf", fileStoredName = "sa.pdf", fileUri = "s3://a"),
                    File(fileId = 11L, fileOriginalName = "b.jpg", fileStoredName = "sb.jpg", fileUri = "s3://b"),
                )
            val score =
                Score(
                    id = scoreId,
                    member = member(),
                    category = category(EvidenceType.EVIDENCE),
                    status = ScoreStatus.PENDING,
                    sourceId = sourceId,
                )
            val evidence =
                Evidence(id = sourceId, title = "t", content = "c", createdAt = now, updatedAt = now, files = files)

            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.evidenceRepo.findById(sourceId) } returns evidence
            justRun { c.s3.execute("s3://a") }
            justRun { c.s3.execute("s3://b") }
            justRun { c.fileRepo.deleteById(10L) }
            justRun { c.fileRepo.deleteById(11L) }
            justRun { c.evidenceRepo.deleteById(sourceId) }
            justRun { c.scoreRepo.deleteById(scoreId) }

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("증빙-파일-S3 삭제가 호출되고 마지막에 점수가 삭제된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 1) { c.evidenceRepo.findById(sourceId) }
                    verify(exactly = 1) { c.s3.execute("s3://a") }
                    verify(exactly = 1) { c.s3.execute("s3://b") }
                    verify(exactly = 1) { c.fileRepo.deleteById(10L) }
                    verify(exactly = 1) { c.fileRepo.deleteById(11L) }
                    verify(exactly = 1) { c.evidenceRepo.deleteById(sourceId) }
                    verify(exactly = 1) { c.scoreRepo.deleteById(scoreId) }
                }
            }
        }

        Given("파일형(FILE) 점수를 삭제할 때 연결된 파일과 S3 객체가 삭제된다") {
            val c = ctx()
            val scoreId = 2L
            val sourceId = 200L
            val file = File(fileId = sourceId, fileOriginalName = "f", fileStoredName = "sf", fileUri = "s3://f")
            val score =
                Score(
                    id = scoreId,
                    member = member(),
                    category = category(EvidenceType.FILE),
                    status = ScoreStatus.PENDING,
                    sourceId = sourceId,
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.fileRepo.findById(sourceId) } returns file
            justRun { c.s3.execute(file.fileUri) }
            justRun { c.fileRepo.deleteById(file.fileId) }
            justRun { c.scoreRepo.deleteById(scoreId) }

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("파일 삭제와 S3 삭제가 호출되고 점수도 삭제된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 1) { c.fileRepo.findById(sourceId) }
                    verify(exactly = 1) { c.s3.execute("s3://f") }
                    verify(exactly = 1) { c.fileRepo.deleteById(sourceId) }
                    verify(exactly = 1) { c.scoreRepo.deleteById(scoreId) }
                }
            }
        }

        Given("파일형(FILE)인데 연결된 파일이 존재하지 않으면 S3/파일 삭제 없이 점수만 삭제된다") {
            val c = ctx()
            val scoreId = 3L
            val sourceId = 300L
            val score =
                Score(
                    id = scoreId,
                    member = member(),
                    category = category(EvidenceType.FILE),
                    status = ScoreStatus.PENDING,
                    sourceId = sourceId,
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.fileRepo.findById(sourceId) } returns null
            justRun { c.scoreRepo.deleteById(scoreId) }

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("S3/파일 삭제는 호출되지 않고 점수만 삭제된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 1) { c.fileRepo.findById(sourceId) }
                    verify(exactly = 0) { c.s3.execute(any()) }
                    verify(exactly = 0) { c.fileRepo.deleteById(any()) }
                    verify(exactly = 1) { c.scoreRepo.deleteById(scoreId) }
                }
            }
        }

        Given("증빙 불필요(UNREQUIRED) 형이면 연결 리소스 삭제 없이 점수만 삭제된다") {
            val c = ctx()
            val scoreId = 4L
            val score =
                Score(
                    id = scoreId,
                    member = member(),
                    category = category(EvidenceType.UNREQUIRED),
                    status = ScoreStatus.PENDING,
                    sourceId = 999L,
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            justRun { c.scoreRepo.deleteById(scoreId) }

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("증빙/파일/S3 호출 없이 점수만 삭제된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 0) { c.evidenceRepo.findById(any()) }
                    verify(exactly = 0) { c.fileRepo.findById(any()) }
                    verify(exactly = 0) { c.s3.execute(any()) }
                    verify(exactly = 1) { c.scoreRepo.deleteById(scoreId) }
                }
            }
        }

        Given("sourceId가 null이면 연결 리소스 없이 점수만 삭제된다") {
            val c = ctx()
            val scoreId = 5L
            val score =
                Score(
                    id = scoreId,
                    member = member(),
                    category = category(EvidenceType.FILE),
                    status = ScoreStatus.PENDING,
                    sourceId = null,
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            justRun { c.scoreRepo.deleteById(scoreId) }

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("연결 리소스 삭제 없이 점수만 삭제된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 0) { c.evidenceRepo.findById(any()) }
                    verify(exactly = 0) { c.fileRepo.findById(any()) }
                    verify(exactly = 0) { c.s3.execute(any()) }
                    verify(exactly = 1) { c.scoreRepo.deleteById(scoreId) }
                }
            }
        }

        Given("점수가 존재하지 않으면 예외가 발생한다") {
            val c = ctx()
            val scoreId = 404L
            every { c.scoreRepo.findById(scoreId) } returns null

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생하며 그 외 호출은 없다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreId) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 0) { c.evidenceRepo.findById(any()) }
                    verify(exactly = 0) { c.fileRepo.findById(any()) }
                    verify(exactly = 0) { c.s3.execute(any()) }
                    verify(exactly = 0) { c.scoreRepo.deleteById(any()) }
                }
            }
        }

        Given("증빙형(EVIDENCE) 점수인데 파일 리스트가 비어있으면 S3/파일 삭제 없이 증빙과 점수만 삭제된다") {
            val c = ctx()
            val scoreId = 6L
            val sourceId = 600L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val score =
                Score(
                    id = scoreId,
                    member = member(),
                    category = category(EvidenceType.EVIDENCE),
                    status = ScoreStatus.PENDING,
                    sourceId = sourceId,
                )
            val evidence =
                Evidence(
                    id = sourceId,
                    title = "t",
                    content = "c",
                    createdAt = now,
                    updatedAt = now,
                    files = emptyList(),
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.evidenceRepo.findById(sourceId) } returns evidence
            justRun { c.evidenceRepo.deleteById(sourceId) }
            justRun { c.scoreRepo.deleteById(scoreId) }

            When("execute를 호출하면") {
                c.service.execute(scoreId)

                Then("S3/파일 삭제 없이 증빙 삭제 후 점수 삭제만 호출된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 1) { c.evidenceRepo.findById(sourceId) }
                    verify(exactly = 0) { c.s3.execute(any()) }
                    verify(exactly = 0) { c.fileRepo.deleteById(any()) }
                    verify(exactly = 1) { c.evidenceRepo.deleteById(sourceId) }
                    verify(exactly = 1) { c.scoreRepo.deleteById(scoreId) }
                }
            }
        }
    })
