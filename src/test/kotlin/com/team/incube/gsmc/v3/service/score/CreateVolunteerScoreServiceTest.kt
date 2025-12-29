package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateVolunteerScoreServiceImpl
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
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class CreateVolunteerScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: CreateVolunteerScoreServiceImpl,
            val student: Member,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            val student =
                Member(
                    id = 1L,
                    name = "Student",
                    email = "student@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            every { currentMemberProvider.getCurrentMember() } returns student

            val service =
                CreateVolunteerScoreServiceImpl(
                    scoreExposedRepository = scoreRepo,
                    currentMemberProvider = currentMemberProvider,
                )
            return TestData(scoreRepo, currentMemberProvider, service, student)
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

        Given("유효한 봉사시간으로 점수를 생성할 때") {
            val c = ctx()
            val value = "24"
            val score = Score(1L, c.student, CategoryType.VOLUNTEER, ScoreStatus.PENDING, null, null, 24.0, null, null)

            every { c.scoreRepo.findByMemberIdAndCategoryType(c.student.id, CategoryType.VOLUNTEER) } returns null
            every { c.scoreRepo.save(any()) } returns score
            every { c.scoreRepo.update(any()) } returns score

            When("execute를 호출하면") {
                val res = c.service.execute(value)

                Then("봉사활동 점수가 PENDING 상태로 생성된다") {
                    res.scoreId shouldBe 1L
                    res.scoreStatus shouldBe ScoreStatus.PENDING
                }
            }
        }

        Given("잘못된 형식의 값으로 생성하려고 할 때") {
            val c = ctx()

            When("execute를 호출하면") {
                Then("SCORE_INVALID_VALUE 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("invalid") }
                    ex.errorCode shouldBe ErrorCode.SCORE_INVALID_VALUE
                }
            }
        }

        Given("0 이하의 값으로 생성하려고 할 때") {
            val c = ctx()

            When("execute를 호출하면") {
                Then("SCORE_VALUE_OUT_OF_RANGE 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("0") }
                    ex.errorCode shouldBe ErrorCode.SCORE_VALUE_OUT_OF_RANGE
                }
            }
        }
    })
