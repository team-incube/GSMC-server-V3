package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateExternalActivityScoreServiceImpl
import com.team.incube.gsmc.v3.domain.score.validator.ScoreLimitValidator
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class CreateExternalActivityScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val fileRepo: FileExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val scoreLimitValidator: ScoreLimitValidator,
            val service: CreateExternalActivityScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val scoreLimitValidator = mockk<ScoreLimitValidator>()

            every { currentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 0L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val service = CreateExternalActivityScoreServiceImpl(scoreRepo, fileRepo, currentMemberProvider, scoreLimitValidator)
            return TestData(scoreRepo, fileRepo, currentMemberProvider, scoreLimitValidator, service)
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

        Given("유효한 외부활동 정보로 점수를 생성할 때") {
            val c = ctx()
            val value = "해커톤 참여"
            val fileId = 100L
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
                    id = 1L,
                    member = member,
                    categoryType = CategoryType.EXTERNAL_ACTIVITY,
                    status = ScoreStatus.PENDING,
                    sourceId = fileId,
                    activityName = value,
                    scoreValue = 1.0,
                    rejectionReason = null,
                )

            every { c.fileRepo.existsById(fileId) } returns true
            justRun { c.scoreLimitValidator.validateScoreLimit(0L, CategoryType.EXTERNAL_ACTIVITY) }
            every { c.scoreRepo.save(any()) } returns score

            When("execute를 호출하면") {
                val res = c.service.execute(value, fileId)

                Then("점수가 생성된다") {
                    res.scoreId shouldBe 1L
                }
            }
        }

        Given("존재하지 않는 파일 ID로 점수를 생성하려고 할 때") {
            val c = ctx()

            every { c.fileRepo.existsById(999L) } returns false

            When("execute를 호출하면") {
                Then("FILE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("활동", 999L) }
                    ex.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }
        }

        Given("점수 제한을 초과하여 생성하려고 할 때") {
            val c = ctx()

            every { c.fileRepo.existsById(100L) } returns true
            every {
                c.scoreLimitValidator.validateScoreLimit(0L, CategoryType.EXTERNAL_ACTIVITY)
            } throws GsmcException(ErrorCode.SCORE_MAX_LIMIT_EXCEEDED)

            When("execute를 호출하면") {
                Then("SCORE_MAX_LIMIT_EXCEEDED 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("활동", 100L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_MAX_LIMIT_EXCEEDED
                }
            }
        }
    })
