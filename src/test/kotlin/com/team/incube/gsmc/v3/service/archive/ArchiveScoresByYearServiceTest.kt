package com.team.incube.gsmc.v3.service.archive

import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchive
import com.team.incube.gsmc.v3.domain.archive.mapper.ScoreArchiveMapper
import com.team.incube.gsmc.v3.domain.archive.service.impl.ArchiveScoresByYearServiceImpl
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import tools.jackson.databind.ObjectMapper
import java.time.Instant

class ArchiveScoresByYearServiceTest :
    BehaviorSpec({
        data class TestContext(
            val scoreRepo: ScoreExposedRepository,
            val archiveMapper: ScoreArchiveMapper,
            val objectMapper: ObjectMapper,
            val service: ArchiveScoresByYearServiceImpl,
        )

        fun ctx(): TestContext {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val archiveMapper = mockk<ScoreArchiveMapper>()
            val objectMapper = ObjectMapper()
            val service = ArchiveScoresByYearServiceImpl(scoreRepo, archiveMapper, objectMapper)
            return TestContext(scoreRepo, archiveMapper, objectMapper, service)
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

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        fun baseScore(
            id: Long = 1L,
            categoryType: CategoryType = CategoryType.CERTIFICATE,
            activityName: String = "정보처리기능사",
        ): Score =
            Score(
                id = id,
                member =
                    Member(
                        id = 1L,
                        name = "테스트",
                        email = "test@gsm.hs.kr",
                        grade = 2,
                        classNumber = 1,
                        number = 1,
                        role = MemberRole.STUDENT,
                    ),
                categoryType = categoryType,
                status = ScoreStatus.APPROVED,
                sourceId = 1L,
                activityName = activityName,
                scoreValue = null,
                rejectionReason = null,
                updatedAt = Instant.now(),
            )

        Given("승인된 누적 점수가 존재할 때") {
            val c = ctx()
            val academicYear = 2024
            val score = baseScore()
            val archivesSlot = slot<List<ScoreArchive>>()

            every { c.archiveMapper.deleteByAcademicYear(academicYear) } returns 0
            every { c.scoreRepo.findAllByStatus(ScoreStatus.APPROVED) } returns listOf(score)
            every { c.archiveMapper.insertBatch(capture(archivesSlot)) } returns 1

            When("execute를 호출하면") {
                val result = c.service.execute(academicYear)

                Then("아카이브가 생성된다") {
                    result shouldBe 1L
                }

                Then("기존 학년도 데이터가 삭제되고 새로 저장된다") {
                    verify(exactly = 1) { c.archiveMapper.deleteByAcademicYear(academicYear) }
                    verify(exactly = 1) { c.scoreRepo.findAllByStatus(ScoreStatus.APPROVED) }
                    verify(exactly = 1) { c.archiveMapper.insertBatch(any()) }
                }

                Then("올바른 데이터로 아카이브가 생성된다") {
                    val captured = archivesSlot.captured
                    captured.size shouldBe 1
                    captured[0].academicYear shouldBe academicYear
                    captured[0].memberEmail shouldBe "test@gsm.hs.kr"
                    captured[0].categoryEnglishName shouldBe "CERTIFICATE"
                }
            }
        }

        Given("승인된 점수가 없을 때") {
            val c = ctx()
            val academicYear = 2024

            every { c.archiveMapper.deleteByAcademicYear(academicYear) } returns 0
            every { c.scoreRepo.findAllByStatus(ScoreStatus.APPROVED) } returns emptyList()

            Then("NO_SCORES_TO_ARCHIVE 예외가 발생한다") {
                val ex = shouldThrow<GsmcException> { c.service.execute(academicYear) }
                ex.errorCode shouldBe ErrorCode.NO_SCORES_TO_ARCHIVE
            }
        }

        Given("isAccumulated가 false인 카테고리만 있을 때") {
            val c = ctx()
            val academicYear = 2024
            val score = baseScore(categoryType = CategoryType.PROJECT_PARTICIPATION)

            every { c.archiveMapper.deleteByAcademicYear(academicYear) } returns 0
            every { c.scoreRepo.findAllByStatus(ScoreStatus.APPROVED) } returns listOf(score)

            Then("NO_SCORES_TO_ARCHIVE 예외가 발생한다") {
                val ex = shouldThrow<GsmcException> { c.service.execute(academicYear) }
                ex.errorCode shouldBe ErrorCode.NO_SCORES_TO_ARCHIVE
            }
        }

        Given("1000개 이상의 점수가 존재할 때") {
            val c = ctx()
            val academicYear = 2024
            val scores = (1L..1500L).map { id -> baseScore(id = id, activityName = "자격증$id") }

            every { c.archiveMapper.deleteByAcademicYear(academicYear) } returns 0
            every { c.scoreRepo.findAllByStatus(ScoreStatus.APPROVED) } returns scores
            every { c.archiveMapper.insertBatch(any()) } returns 1000 andThen 500

            When("execute를 호출하면") {
                val result = c.service.execute(academicYear)

                Then("배치로 나누어 저장된다") {
                    result shouldBe 1500L
                    verify(exactly = 1) { c.archiveMapper.deleteByAcademicYear(academicYear) }
                    verify(exactly = 2) { c.archiveMapper.insertBatch(any()) }
                }
            }
        }
    })
