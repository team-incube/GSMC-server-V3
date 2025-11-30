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
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class CreateVolunteerScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: CreateVolunteerScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            every { currentMemberProvider.getCurrentMember() } returns
                Member(0L, "Test User", "test@test.com", 1, 1, 1, MemberRole.STUDENT)

            val service = CreateVolunteerScoreServiceImpl(scoreRepo, currentMemberProvider)
            return TestData(scoreRepo, currentMemberProvider, service)
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

        Given("유효한 봉사시간으로 점수를 생성할 때") {
            val c = ctx()
            val value = "24"
            val member = Member(0L, "Test User", "test@test.com", 1, 1, 1, MemberRole.STUDENT)
            val score = Score(1L, member, CategoryType.VOLUNTEER, ScoreStatus.PENDING, null, null, 24.0, null)

            every { c.scoreRepo.findByMemberIdAndCategoryType(0L, CategoryType.VOLUNTEER) } returns null
            every { c.scoreRepo.save(any()) } returns score

            When("execute를 호출하면") {
                val res = c.service.execute(value)

                Then("봉사활동 점수가 생성된다") {
                    res.scoreId shouldBe 1L
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
