package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.CreateAcademicGradeScoreServiceImpl
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

class CreateAcademicGradeScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val memberRepo: MemberExposedRepository,
            val eventPublisher: ApplicationEventPublisher,
            val service: CreateAcademicGradeScoreServiceImpl,
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
                CreateAcademicGradeScoreServiceImpl(
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

        Given("유효한 등급으로 교과성적 점수를 생성할 때") {
            val c = ctx()
            val value = "1.5"
            val member = Member(0L, "Test User", "test@test.com", 1, 1, 1, MemberRole.STUDENT)
            val score = Score(1L, member, CategoryType.ACADEMIC_GRADE, ScoreStatus.APPROVED, null, null, 1.5, null, null)

            every { c.memberRepo.findById(member.id) } returns member
            every { c.scoreRepo.findByMemberIdAndCategoryType(member.id, CategoryType.ACADEMIC_GRADE) } returns null
            every { c.scoreRepo.save(any()) } returns score
            every { c.scoreRepo.update(any()) } returns score

            When("execute를 호출하면") {
                val res = c.service.execute(value, member.id)

                Then("교과성적 점수가 APPROVED 상태로 생성된다") {
                    res.scoreId shouldBe 1L
                    res.scoreStatus shouldBe ScoreStatus.APPROVED
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

        Given("범위를 벗어난 값으로 생성하려고 할 때") {
            val c = ctx()

            When("0.5 값으로 execute를 호출하면") {
                Then("SCORE_VALUE_OUT_OF_RANGE 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("0.5", 0L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_VALUE_OUT_OF_RANGE
                }
            }

            When("9.5 값으로 execute를 호출하면") {
                Then("SCORE_VALUE_OUT_OF_RANGE 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute("9.5", 0L) }
                    ex.errorCode shouldBe ErrorCode.SCORE_VALUE_OUT_OF_RANGE
                }
            }
        }

        Given("담임 선생님이 다른 학급 학생의 점수를 생성하려고 할 때") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val memberRepo = mockk<MemberExposedRepository>()
            val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            every { currentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 1L,
                    name = "Homeroom Teacher",
                    email = "homeroom@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 0,
                    role = MemberRole.HOMEROOM_TEACHER,
                )

            val service =
                CreateAcademicGradeScoreServiceImpl(
                    scoreExposedRepository = scoreRepo,
                    currentMemberProvider = currentMemberProvider,
                    eventPublisher = eventPublisher,
                    memberExposedRepository = memberRepo,
                )

            val value = "2.0"
            val otherClassStudent = Member(2L, "Other Student", "other@test.com", 2, 1, 1, MemberRole.STUDENT)

            every { memberRepo.findById(otherClassStudent.id) } returns otherClassStudent

            When("execute를 호출하면") {
                Then("NOT_ASSIGNED_HOMEROOM_CLASS 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { service.execute(value, otherClassStudent.id) }
                    ex.errorCode shouldBe ErrorCode.NOT_ASSIGNED_HOMEROOM_CLASS
                }
            }
        }

        Given("담임 선생님이 자신의 학급 학생의 점수를 생성할 때") {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val memberRepo = mockk<MemberExposedRepository>()
            val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            every { currentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 1L,
                    name = "Homeroom Teacher",
                    email = "homeroom@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 0,
                    role = MemberRole.HOMEROOM_TEACHER,
                )

            val service =
                CreateAcademicGradeScoreServiceImpl(
                    scoreExposedRepository = scoreRepo,
                    currentMemberProvider = currentMemberProvider,
                    eventPublisher = eventPublisher,
                    memberExposedRepository = memberRepo,
                )

            val value = "2.0"
            val sameClassStudent = Member(2L, "Same Class Student", "same@test.com", 1, 1, 1, MemberRole.STUDENT)
            val score = Score(1L, sameClassStudent, CategoryType.ACADEMIC_GRADE, ScoreStatus.APPROVED, null, null, 2.0, null, null)

            every { memberRepo.findById(sameClassStudent.id) } returns sameClassStudent
            every { scoreRepo.findByMemberIdAndCategoryType(sameClassStudent.id, CategoryType.ACADEMIC_GRADE) } returns null
            every { scoreRepo.save(any()) } returns score

            When("execute를 호출하면") {
                val res = service.execute(value, sameClassStudent.id)

                Then("교과성적 점수가 정상적으로 생성된다") {
                    res.scoreId shouldBe 1L
                    res.scoreStatus shouldBe ScoreStatus.APPROVED
                }
            }
        }
    })
