package com.team.incube.gsmc.v3.service.archive

import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchive
import com.team.incube.gsmc.v3.domain.archive.mapper.ScoreArchiveMapper
import com.team.incube.gsmc.v3.domain.archive.service.impl.FindArchivedScoresByYearServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class FindArchivedScoresByYearServiceTest :
    BehaviorSpec({
        data class TestContext(
            val archiveMapper: ScoreArchiveMapper,
            val service: FindArchivedScoresByYearServiceImpl,
        )

        fun ctx(): TestContext {
            val archiveMapper = mockk<ScoreArchiveMapper>()
            val service = FindArchivedScoresByYearServiceImpl(archiveMapper)
            return TestContext(archiveMapper, service)
        }

        fun baseArchive(
            archiveId: Long = 1L,
            academicYear: Int = 2024,
            memberEmail: String = "test@gsm.hs.kr",
            categoryName: String = "CERTIFICATE",
        ): ScoreArchive =
            ScoreArchive(
                archiveId = archiveId,
                academicYear = academicYear,
                archivedAt = Instant.now(),
                memberEmail = memberEmail,
                memberName = "테스트",
                memberGrade = 2,
                memberClassNumber = 1,
                memberNumber = 1,
                categoryEnglishName = categoryName,
                categoryKoreanName = "자격증",
                scoreSnapshot = """{"scoreId":1,"status":"APPROVED"}""",
            )

        Given("아카이브된 데이터가 존재할 때") {
            val c = ctx()
            val academicYear = 2024
            val archives =
                listOf(
                    baseArchive(archiveId = 1L, memberEmail = "test1@gsm.hs.kr"),
                    baseArchive(archiveId = 2L, memberEmail = "test2@gsm.hs.kr", categoryName = "TOPCIT"),
                )

            every { c.archiveMapper.findByAcademicYear(academicYear) } returns archives

            When("execute를 호출하면") {
                val result = c.service.execute(academicYear)

                Then("아카이브 목록이 반환된다") {
                    result.size shouldBe 2
                    result[0].memberEmail shouldBe "test1@gsm.hs.kr"
                    result[1].memberEmail shouldBe "test2@gsm.hs.kr"
                }

                Then("Mapper가 호출된다") {
                    verify(exactly = 1) { c.archiveMapper.findByAcademicYear(academicYear) }
                }
            }
        }

        Given("아카이브된 데이터가 없을 때") {
            val c = ctx()
            val academicYear = 2024

            every { c.archiveMapper.findByAcademicYear(academicYear) } returns emptyList()

            Then("ARCHIVE_NOT_FOUND 예외가 발생한다") {
                val ex = shouldThrow<GsmcException> { c.service.execute(academicYear) }
                ex.errorCode shouldBe ErrorCode.ARCHIVE_NOT_FOUND
            }
        }
    })