package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateProjectParticipationServiceImpl
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
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class CreateProjectParticipationServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val projectRepo: ProjectExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val scoreLimitValidator: ScoreLimitValidator,
            val service: CreateProjectParticipationServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val projectRepo = mockk<ProjectExposedRepository>()
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

            val service = CreateProjectParticipationServiceImpl(scoreRepo, projectRepo, currentMemberProvider, scoreLimitValidator)
            return TestData(scoreRepo, projectRepo, currentMemberProvider, scoreLimitValidator, service)
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

        Given("유효한 프로젝트로 참여 점수를 생성할 때") {
            val c = ctx()
            val projectId = 100L
            val projectTitle = "프로젝트 제목"
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
                    categoryType = CategoryType.PROJECT_PARTICIPATION,
                    status = ScoreStatus.PENDING,
                    sourceId = projectId,
                    activityName = projectTitle,
                    scoreValue = 2.0,
                    rejectionReason = null,
                )

            every {
                c.projectRepo.findProjectTitleAndValidateParticipant(projectId, 0L)
            } returns projectTitle
            every {
                c.scoreRepo.existsProjectParticipationScore(
                    memberId = 0L,
                    projectId = projectId,
                    projectTitle = projectTitle,
                )
            } returns false
            justRun { c.scoreLimitValidator.validateScoreLimit(0L, CategoryType.PROJECT_PARTICIPATION) }
            every { c.scoreRepo.save(any()) } returns score

            When("execute를 호출하면") {
                val res = c.service.execute(projectId)

                Then("프로젝트 참여 점수가 생성된다") {
                    res.scoreId shouldBe 1L
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) {
                        c.projectRepo.findProjectTitleAndValidateParticipant(projectId, 0L)
                    }
                    verify(exactly = 1) {
                        c.scoreRepo.existsProjectParticipationScore(
                            memberId = 0L,
                            projectId = projectId,
                            projectTitle = projectTitle,
                        )
                    }
                    verify(exactly = 1) { c.scoreLimitValidator.validateScoreLimit(0L, CategoryType.PROJECT_PARTICIPATION) }
                    verify(exactly = 1) { c.scoreRepo.save(any()) }
                }
            }
        }

        Given("존재하지 않는 프로젝트로 점수를 생성하려고 할 때") {
            val c = ctx()
            val projectId = 999L

            every {
                c.projectRepo.findProjectTitleAndValidateParticipant(projectId, 0L)
            } returns null
            every { c.projectRepo.findProjectTitleById(projectId) } returns null

            When("execute를 호출하면") {
                Then("PROJECT_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId) }
                    ex.errorCode shouldBe ErrorCode.PROJECT_NOT_FOUND
                }
            }
        }

        Given("프로젝트 참가자가 아닌 사용자가 점수를 생성하려고 할 때") {
            val c = ctx()
            val projectId = 100L

            every {
                c.projectRepo.findProjectTitleAndValidateParticipant(projectId, 0L)
            } returns null
            every { c.projectRepo.findProjectTitleById(projectId) } returns "프로젝트 제목"

            When("execute를 호출하면") {
                Then("NOT_PROJECT_PARTICIPANT 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId) }
                    ex.errorCode shouldBe ErrorCode.NOT_PROJECT_PARTICIPANT
                }
            }
        }

        Given("이미 점수가 등록된 프로젝트로 점수를 생성하려고 할 때") {
            val c = ctx()
            val projectId = 100L
            val projectTitle = "프로젝트 제목"

            every {
                c.projectRepo.findProjectTitleAndValidateParticipant(projectId, 0L)
            } returns projectTitle
            every {
                c.scoreRepo.existsProjectParticipationScore(
                    memberId = 0L,
                    projectId = projectId,
                    projectTitle = projectTitle,
                )
            } returns true

            When("execute를 호출하면") {
                Then("SCORE_ALREADY_EXISTS 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId) }
                    ex.errorCode shouldBe ErrorCode.SCORE_ALREADY_EXISTS
                }
            }
        }

        Given("점수 제한을 초과하여 생성하려고 할 때") {
            When("execute를 호출하면") {
                val c = ctx()
                val projectId = 100L
                val projectTitle = "프로젝트 제목"

                every {
                    c.projectRepo.findProjectTitleAndValidateParticipant(projectId, 0L)
                } returns projectTitle
                every {
                    c.scoreRepo.existsProjectParticipationScore(
                        memberId = 0L,
                        projectId = projectId,
                        projectTitle = projectTitle,
                    )
                } returns false
                every {
                    c.scoreLimitValidator.validateScoreLimit(0L, CategoryType.PROJECT_PARTICIPATION)
                } throws GsmcException(ErrorCode.SCORE_MAX_LIMIT_EXCEEDED)

                Then("SCORE_MAX_LIMIT_EXCEEDED 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(projectId) }
                    ex.errorCode shouldBe ErrorCode.SCORE_MAX_LIMIT_EXCEEDED
                }
            }
        }
    })
