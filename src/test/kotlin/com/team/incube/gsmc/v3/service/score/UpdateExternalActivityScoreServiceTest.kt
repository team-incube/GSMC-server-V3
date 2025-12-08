package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.UpdateExternalActivityScoreServiceImpl
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

class UpdateExternalActivityScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val fileRepo: FileExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: UpdateExternalActivityScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val fileRepo = mockk<FileExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()

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

            val service = UpdateExternalActivityScoreServiceImpl(scoreRepo, fileRepo, currentMemberProvider)
            return TestData(scoreRepo, fileRepo, currentMemberProvider, service)
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

        Given("외부활동 점수를 업데이트할 때") {
            val c = ctx()
            val scoreId = 1L
            val value = "해커톤 참여 수정"
            val fileId = 200L
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
            val existingScore =
                Score(
                    id = scoreId,
                    member = member,
                    categoryType = CategoryType.EXTERNAL_ACTIVITY,
                    status = ScoreStatus.APPROVED,
                    sourceId = 100L,
                    activityName = "해커톤 참여",
                    scoreValue = 1.0,
                    rejectionReason = null,
                )
            val updatedScore =
                Score(
                    id = scoreId,
                    member = member,
                    categoryType = CategoryType.EXTERNAL_ACTIVITY,
                    status = ScoreStatus.PENDING,
                    sourceId = fileId,
                    activityName = value,
                    scoreValue = 1.0,
                    rejectionReason = null,
                )

            every { c.scoreRepo.findById(scoreId) } returns existingScore
            every { c.fileRepo.existsById(fileId) } returns true
            every { c.scoreRepo.update(any()) } returns updatedScore

            When("execute를 호출하면") {
                val res = c.service.execute(scoreId, value, fileId)

                Then("점수가 업데이트되고 상태가 PENDING으로 변경된다") {
                    res.scoreId shouldBe scoreId
                    res.activityName shouldBe value
                    res.scoreStatus shouldBe ScoreStatus.PENDING
                }
            }
        }

        Given("존재하지 않는 점수를 업데이트하려고 할 때") {
            val c = ctx()

            every { c.scoreRepo.findById(999L) } returns null

            When("execute를 호출하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(999L, "수정", 100L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }

        Given("다른 사용자의 점수를 업데이트하려고 할 때") {
            val c = ctx()
            val otherMember =
                Member(
                    id = 99L,
                    name = "Other User",
                    email = "other@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 2,
                    role = MemberRole.STUDENT,
                )
            val score =
                Score(
                    id = 1L,
                    member = otherMember,
                    categoryType = CategoryType.EXTERNAL_ACTIVITY,
                    status = ScoreStatus.APPROVED,
                    sourceId = 100L,
                    activityName = "활동",
                    scoreValue = 1.0,
                    rejectionReason = null,
                )

            every { c.scoreRepo.findById(1L) } returns score

            When("execute를 호출하면") {
                Then("SCORE_NOT_OWNED 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(1L, "수정", 100L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_NOT_OWNED
                }
            }
        }

        Given("잘못된 카테고리의 점수를 업데이트하려고 할 때") {
            val c = ctx()
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
                    categoryType = CategoryType.AWARD,
                    status = ScoreStatus.APPROVED,
                    sourceId = 100L,
                    activityName = "수상",
                    scoreValue = 1.0,
                    rejectionReason = null,
                )

            every { c.scoreRepo.findById(1L) } returns score

            When("execute를 호출하면") {
                Then("SCORE_INVALID_CATEGORY 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(1L, "수정", 100L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_INVALID_CATEGORY
                }
            }
        }

        Given("존재하지 않는 파일로 업데이트하려고 할 때") {
            val c = ctx()
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
                    status = ScoreStatus.APPROVED,
                    sourceId = 100L,
                    activityName = "활동",
                    scoreValue = 1.0,
                    rejectionReason = null,
                )

            every { c.scoreRepo.findById(1L) } returns score
            every { c.fileRepo.existsById(999L) } returns false

            When("execute를 호출하면") {
                Then("FILE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(1L, "수정", 999L) }
                    ex.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }
        }
    })
