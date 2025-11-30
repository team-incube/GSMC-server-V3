package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateJlptScoreServiceImpl
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateNcsScoreServiceImpl
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateNewrrowSchoolScoreServiceImpl
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateReadAThonScoreServiceImpl
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateToeicAcademyScoreServiceImpl
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateTopcitScoreServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class CreateOtherScoreServicesTest :
    BehaviorSpec({
        val member = Member(0L, "Test User", "test@test.com", 1, 1, 1, MemberRole.STUDENT)

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

        // JLPT Test
        Given("JLPT 점수 생성") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns member
            val service = CreateJlptScoreServiceImpl(scoreRepo, fileRepo, currentMemberProvider)

            every { fileRepo.existsById(100L) } returns true
            every { scoreRepo.findByMemberIdAndCategoryType(0L, CategoryType.JLPT) } returns null
            every { scoreRepo.save(any()) } returns Score(1L, member, CategoryType.JLPT, ScoreStatus.PENDING, 100L, null, 3.0, null)

            When("유효한 값으로 실행하면") {
                val res = service.execute("3", 100L)
                Then("점수가 생성된다") { res.scoreId shouldBe 1L }
            }

            When("범위 초과 값으로 실행하면") {
                Then("예외 발생") {
                    shouldThrow<GsmcException> { service.execute("6", 100L) }.errorCode shouldBe ErrorCode.SCORE_VALUE_OUT_OF_RANGE
                }
            }
        }

        // TOPCIT Test
        Given("TOPCIT 점수 생성") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns member
            val service = CreateTopcitScoreServiceImpl(scoreRepo, fileRepo, currentMemberProvider)

            every { fileRepo.existsById(100L) } returns true
            every { scoreRepo.findByMemberIdAndCategoryType(0L, CategoryType.TOPCIT) } returns null
            every { scoreRepo.save(any()) } returns Score(1L, member, CategoryType.TOPCIT, ScoreStatus.PENDING, 100L, null, 500.0, null)

            When("유효한 값으로 실행하면") {
                val res = service.execute("500", 100L)
                Then("점수가 생성된다") { res.scoreId shouldBe 1L }
            }

            When("범위 초과 값으로 실행하면") {
                Then("예외 발생") {
                    shouldThrow<GsmcException> { service.execute("1001", 100L) }.errorCode shouldBe ErrorCode.SCORE_VALUE_OUT_OF_RANGE
                }
            }
        }

        // NCS Test
        Given("NCS 점수 생성") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns member
            val service = CreateNcsScoreServiceImpl(scoreRepo, fileRepo, currentMemberProvider)

            every { fileRepo.existsById(100L) } returns true
            every { scoreRepo.findByMemberIdAndCategoryType(0L, CategoryType.NCS) } returns null
            every { scoreRepo.save(any()) } returns Score(1L, member, CategoryType.NCS, ScoreStatus.PENDING, 100L, null, 3.5, null)

            When("유효한 값으로 실행하면") {
                val res = service.execute("3.5", 100L)
                Then("점수가 생성된다") { res.scoreId shouldBe 1L }
            }
        }

        // ReadAThon Test
        Given("독서마라톤 점수 생성") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns member
            val service = CreateReadAThonScoreServiceImpl(scoreRepo, fileRepo, currentMemberProvider)

            every { fileRepo.existsById(100L) } returns true
            every { scoreRepo.findByMemberIdAndCategoryType(0L, CategoryType.READ_A_THON) } returns null
            every { scoreRepo.save(any()) } returns Score(1L, member, CategoryType.READ_A_THON, ScoreStatus.PENDING, 100L, null, 5.0, null)

            When("유효한 값으로 실행하면") {
                val res = service.execute("5", 100L)
                Then("점수가 생성된다") { res.scoreId shouldBe 1L }
            }
        }

        // NewrrowSchool Test
        Given("뉴로우스쿨 점수 생성") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns member
            val service = CreateNewrrowSchoolScoreServiceImpl(scoreRepo, fileRepo, currentMemberProvider)

            every { fileRepo.existsById(100L) } returns true
            every { scoreRepo.findByMemberIdAndCategoryType(0L, CategoryType.NEWRROW_SCHOOL) } returns null
            every { scoreRepo.save(any()) } returns
                Score(1L, member, CategoryType.NEWRROW_SCHOOL, ScoreStatus.PENDING, 100L, null, 85.0, null)

            When("유효한 값으로 실행하면") {
                val res = service.execute("85", 100L)
                Then("점수가 생성된다") { res.scoreId shouldBe 1L }
            }
        }

        // ToeicAcademy Test
        Given("토익사관학교 점수 생성") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns member
            val service = CreateToeicAcademyScoreServiceImpl(scoreRepo, currentMemberProvider)

            every { scoreRepo.findByMemberIdAndCategoryType(0L, CategoryType.TOEIC_ACADEMY) } returns null
            every { scoreRepo.save(any()) } returns
                Score(1L, member, CategoryType.TOEIC_ACADEMY, ScoreStatus.PENDING, null, null, null, null)

            When("실행하면") {
                val res = service.execute()
                Then("점수가 생성된다") { res.scoreId shouldBe 1L }
            }
        }
    })
