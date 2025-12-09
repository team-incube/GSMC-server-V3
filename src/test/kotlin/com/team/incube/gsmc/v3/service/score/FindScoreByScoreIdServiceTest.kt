package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.FindScoreByScoreIdServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class FindScoreByScoreIdServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val evidenceRepo: EvidenceExposedRepository,
            val fileRepo: FileExposedRepository,
            val service: FindScoreByScoreIdServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val evidenceRepo = mockk<EvidenceExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val service = FindScoreByScoreIdServiceImpl(scoreRepo, evidenceRepo, fileRepo)
            return TestData(scoreRepo, evidenceRepo, fileRepo, service)
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

        Given("EVIDENCE 타입의 점수를 조회할 때") {
            val c = ctx()
            val now = LocalDateTime.of(2025, 11, 28, 12, 0, 0)
            val scoreId = 1L
            val member =
                Member(
                    id = 0L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )
            val score =
                Score(
                    id = scoreId,
                    member = member,
                    categoryType = CategoryType.PROJECT_PARTICIPATION,
                    status = ScoreStatus.APPROVED,
                    sourceId = 100L,
                    activityName = "프로젝트1",
                    scoreValue = 10.0,
                    rejectionReason = null,
                )
            val files =
                listOf(
                    File(
                        id = 10L,
                        member = 0L,
                        originalName = "a.pdf",
                        storeName = "s-a.pdf",
                        uri = "uri-a",
                    ),
                )
            val evidence =
                Evidence(
                    id = 100L,
                    member = 0L,
                    title = "증빙 제목",
                    content = "증빙 내용",
                    createdAt = now,
                    updatedAt = now,
                    files = files,
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.evidenceRepo.findById(100L) } returns evidence

            When("execute를 호출하면") {
                val res = c.service.execute(scoreId)

                Then("점수와 증빙 정보가 반환된다") {
                    res.scoreId shouldBe scoreId
                    res.activityName shouldBe "프로젝트1"
                    res.scoreValue shouldBe 10.0
                    res.scoreStatus shouldBe ScoreStatus.APPROVED
                    res.evidence shouldNotBe null
                    res.evidence!!.evidenceId shouldBe 100L
                    res.evidence!!.title shouldBe "증빙 제목"
                    res.evidence!!.content shouldBe "증빙 내용"
                    res.evidence!!.files.size shouldBe 1
                    res.file shouldBe null
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 1) { c.evidenceRepo.findById(100L) }
                }
            }
        }

        Given("FILE 타입의 점수를 조회할 때") {
            val c = ctx()
            val scoreId = 2L
            val member =
                Member(
                    id = 0L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )
            val score =
                Score(
                    id = scoreId,
                    member = member,
                    categoryType = CategoryType.AWARD,
                    status = ScoreStatus.PENDING,
                    sourceId = 200L,
                    activityName = "수상1",
                    scoreValue = 5.0,
                    rejectionReason = null,
                )
            val file =
                File(
                    id = 200L,
                    member = 0L,
                    originalName = "award.pdf",
                    storeName = "s-award.pdf",
                    uri = "uri-award",
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.fileRepo.findById(200L) } returns file

            When("execute를 호출하면") {
                val res = c.service.execute(scoreId)

                Then("점수와 파일 정보가 반환된다") {
                    res.scoreId shouldBe scoreId
                    res.activityName shouldBe "수상1"
                    res.scoreValue shouldBe 5.0
                    res.scoreStatus shouldBe ScoreStatus.PENDING
                    res.evidence shouldBe null
                    res.file shouldNotBe null
                    res.file!!.id shouldBe 200L
                    res.file!!.originalName shouldBe "award.pdf"
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 1) { c.fileRepo.findById(200L) }
                }
            }
        }

        Given("UNREQUIRED 타입의 점수를 조회할 때") {
            val c = ctx()
            val scoreId = 3L
            val member =
                Member(
                    id = 0L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )
            val score =
                Score(
                    id = scoreId,
                    member = member,
                    categoryType = CategoryType.ACADEMIC_GRADE,
                    status = ScoreStatus.APPROVED,
                    sourceId = null,
                    activityName = "교과성적",
                    scoreValue = 95.0,
                    rejectionReason = null,
                )

            every { c.scoreRepo.findById(scoreId) } returns score

            When("execute를 호출하면") {
                val res = c.service.execute(scoreId)

                Then("점수 정보만 반환되고 증빙과 파일은 null이다") {
                    res.scoreId shouldBe scoreId
                    res.activityName shouldBe "교과성적"
                    res.scoreValue shouldBe 95.0
                    res.scoreStatus shouldBe ScoreStatus.APPROVED
                    res.evidence shouldBe null
                    res.file shouldBe null
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) { c.scoreRepo.findById(scoreId) }
                    verify(exactly = 0) { c.evidenceRepo.findById(any()) }
                    verify(exactly = 0) { c.fileRepo.findById(any()) }
                }
            }
        }

        Given("존재하지 않는 scoreId로 조회할 때") {
            val c = ctx()
            val scoreId = 999L

            every { c.scoreRepo.findById(scoreId) } returns null

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(scoreId) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }

        Given("REJECTED 상태의 점수를 조회할 때") {
            val c = ctx()
            val scoreId = 4L
            val member =
                Member(
                    id = 0L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )
            val score =
                Score(
                    id = scoreId,
                    member = member,
                    categoryType = CategoryType.CERTIFICATE,
                    status = ScoreStatus.REJECTED,
                    sourceId = 300L,
                    activityName = "자격증1",
                    scoreValue = 0.0,
                    rejectionReason = "증빙이 불충분합니다.",
                )

            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.fileRepo.findById(300L) } returns
                File(
                    id = 300L,
                    member = 0L,
                    originalName = "cert.pdf",
                    storeName = "s-cert.pdf",
                    uri = "uri-cert",
                )

            When("execute를 호출하면") {
                val res = c.service.execute(scoreId)

                Then("거부 사유가 포함된 점수 정보가 반환된다") {
                    res.scoreId shouldBe scoreId
                    res.scoreStatus shouldBe ScoreStatus.REJECTED
                    res.rejectionReason shouldBe "증빙이 불충분합니다."
                }
            }
        }
    })
