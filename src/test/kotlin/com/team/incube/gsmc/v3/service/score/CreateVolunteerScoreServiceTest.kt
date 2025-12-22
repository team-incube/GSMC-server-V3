package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
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
import org.springframework.context.ApplicationEventPublisher

class CreateVolunteerScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val memberRepo: MemberExposedRepository,
            val eventPublisher: ApplicationEventPublisher,
            val service: CreateVolunteerScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val memberRepo = mockk<MemberExposedRepository>()
            val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

            every { currentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 1L,
                    name = "Teacher",
                    email = "teacher@test.com",
                    grade = 0,
                    classNumber = 0,
                    number = 0,
                    role = MemberRole.TEACHER,
                )

            val service =
                CreateVolunteerScoreServiceImpl(
                    scoreExposedRepository = scoreRepo,
                    currentMemberProvider = currentMemberProvider,
                    eventPublisher = eventPublisher,
                    memberExposedRepository = memberRepo,
                )
            return TestData(scoreRepo, currentMemberProvider, memberRepo, eventPublisher, service)
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
            val member = Member(0L, "Test User", "test@test.com", 1, 1, 1, MemberRole.STUDENT)
            val score = Score(1L, member, CategoryType.VOLUNTEER, ScoreStatus.PENDING, null, null, 24.0, null, null)

            every { c.memberRepo.findById(member.id) } returns member
            every { c.scoreRepo.findByMemberIdAndCategoryType(member.id, CategoryType.VOLUNTEER) } returns
                Score(1L, member, CategoryType.VOLUNTEER, ScoreStatus.PENDING, null, null, 10.0, null, null)
            every { c.scoreRepo.save(any()) } returns score
            every { c.scoreRepo.update(any()) } returns score

            When("execute를 호출하면") {
                val res = c.service.execute(value, member.id)

                Then("봉사활동 점수가 생성된다") {
                    res.scoreId shouldBe 1L
                }
            }
        }

        Given("잘못된 형식의 값으로 생성하려고 할 때") {
            val c = ctx()

            When("execute를 호출하면") {
                Then("SCORE_INVALID_VALUE 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("invalid", 0L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_INVALID_VALUE
                }
            }
        }

        Given("0 이하의 값으로 생성하려고 할 때") {
            val c = ctx()

            When("execute를 호출하면") {
                Then("SCORE_VALUE_OUT_OF_RANGE 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("0", 0L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_VALUE_OUT_OF_RANGE
                }
            }
        }
    })
