package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.calculator.TotalScoreCalculator
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.FindMyPercentInGradeServiceImpl
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

class FindMyPercentInGradeServiceTest :
    BehaviorSpec({
        data class TestData(
            val currentMemberProvider: CurrentMemberProvider,
            val memberRepo: MemberExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val totalScoreCalculator: TotalScoreCalculator,
            val service: FindMyPercentInGradeServiceImpl,
        )

        fun ctx(): TestData {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val memberRepo = mockk<MemberExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val totalScoreCalculator = mockk<TotalScoreCalculator>()

            val service =
                FindMyPercentInGradeServiceImpl(
                    currentMemberProvider = currentMemberProvider,
                    memberExposedRepository = memberRepo,
                    scoreExposedRepository = scoreRepo,
                    totalScoreCalculator = totalScoreCalculator,
                )

            return TestData(currentMemberProvider, memberRepo, scoreRepo, totalScoreCalculator, service)
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

        Given("학년 정보가 없는 학생인 경우") {
            val c = ctx()

            val memberWithoutGrade =
                Member(
                    id = 1L,
                    name = "학생1",
                    email = "student1@test.com",
                    grade = null,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns memberWithoutGrade

            When("백분위수 조회를 시도하면") {
                Then("MEMBER_GRADE_CLASS_NOT_SET 예외가 발생한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            c.service.execute()
                        }
                    exception.errorCode shouldBe ErrorCode.MEMBER_GRADE_CLASS_NOT_SET
                }
            }
        }

        Given("학년에 학생이 1명만 있는 경우") {
            val c = ctx()

            val currentMember =
                Member(
                    id = 1L,
                    name = "학생1",
                    email = "student1@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns currentMember
            every { c.memberRepo.findStudentsByGrade(1) } returns listOf(currentMember)

            When("백분위수 조회를 하면") {
                val result = c.service.execute()

                Then("상위 100%, 하위 0%가 반환된다") {
                    result.topPercentile shouldBe 100.0
                    result.bottomPercentile shouldBe 0.0
                }
            }
        }

        Given("학년에 여러 반의 학생들이 있고 현재 학생이 중간 순위인 경우") {
            val c = ctx()

            val student1Class1 =
                Member(
                    id = 1L,
                    name = "1반 학생1",
                    email = "student1@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student2Class1 =
                Member(
                    id = 2L,
                    name = "1반 학생2",
                    email = "student2@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 2,
                    role = MemberRole.STUDENT,
                )

            val student1Class2 =
                Member(
                    id = 3L,
                    name = "2반 학생1",
                    email = "student3@test.com",
                    grade = 1,
                    classNumber = 2,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student2Class2 =
                Member(
                    id = 4L,
                    name = "2반 학생2",
                    email = "student4@test.com",
                    grade = 1,
                    classNumber = 2,
                    number = 2,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1Class1, student2Class1, student1Class2, student2Class2)

            every { c.currentMemberProvider.getCurrentMember() } returns student2Class1
            every { c.memberRepo.findStudentsByGrade(1) } returns students
            every { c.scoreRepo.findApprovedScoresByMemberIds(listOf(1L, 2L, 3L, 4L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(60, 75, 80, 90)

            When("백분위수 조회를 하면") {
                val result = c.service.execute()

                Then("상위 25%, 하위 75%가 반환된다") {
                    result.topPercentile shouldBe 25.0
                    result.bottomPercentile shouldBe 75.0
                }
            }
        }

        Given("학년에 여러 학생이 있고 현재 학생이 1등인 경우") {
            val c = ctx()

            val student1 =
                Member(
                    id = 1L,
                    name = "학생1",
                    email = "student1@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student2 =
                Member(
                    id = 2L,
                    name = "학생2",
                    email = "student2@test.com",
                    grade = 1,
                    classNumber = 2,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student3 =
                Member(
                    id = 3L,
                    name = "학생3",
                    email = "student3@test.com",
                    grade = 1,
                    classNumber = 3,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1, student2, student3)

            every { c.currentMemberProvider.getCurrentMember() } returns student1
            every { c.memberRepo.findStudentsByGrade(1) } returns students
            every { c.scoreRepo.findApprovedScoresByMemberIds(listOf(1L, 2L, 3L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(100, 75, 50)

            When("백분위수 조회를 하면") {
                val result = c.service.execute()

                Then("상위 약 66.67%, 하위 약 33.33%가 반환된다") {
                    result.topPercentile shouldBe 66.66666666666666
                    result.bottomPercentile shouldBe 33.33333333333334
                }
            }
        }

        Given("학년에 여러 학생이 있고 현재 학생이 꼴등인 경우") {
            val c = ctx()

            val student1 =
                Member(
                    id = 1L,
                    name = "학생1",
                    email = "student1@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student2 =
                Member(
                    id = 2L,
                    name = "학생2",
                    email = "student2@test.com",
                    grade = 1,
                    classNumber = 2,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student3 =
                Member(
                    id = 3L,
                    name = "학생3",
                    email = "student3@test.com",
                    grade = 1,
                    classNumber = 3,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1, student2, student3)

            every { c.currentMemberProvider.getCurrentMember() } returns student3
            every { c.memberRepo.findStudentsByGrade(1) } returns students
            every { c.scoreRepo.findApprovedScoresByMemberIds(listOf(1L, 2L, 3L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(100, 75, 50)

            When("백분위수 조회를 하면") {
                val result = c.service.execute()

                Then("상위 0%, 하위 100%가 반환된다") {
                    result.topPercentile shouldBe 0.0
                    result.bottomPercentile shouldBe 100.0
                }
            }
        }

        Given("학년에 동점자가 여러 명 있는 경우") {
            val c = ctx()

            val student1 =
                Member(
                    id = 1L,
                    name = "학생1",
                    email = "student1@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student2 =
                Member(
                    id = 2L,
                    name = "학생2",
                    email = "student2@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 2,
                    role = MemberRole.STUDENT,
                )

            val student3 =
                Member(
                    id = 3L,
                    name = "학생3",
                    email = "student3@test.com",
                    grade = 1,
                    classNumber = 2,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val student4 =
                Member(
                    id = 4L,
                    name = "학생4",
                    email = "student4@test.com",
                    grade = 1,
                    classNumber = 2,
                    number = 2,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1, student2, student3, student4)

            every { c.currentMemberProvider.getCurrentMember() } returns student2
            every { c.memberRepo.findStudentsByGrade(1) } returns students
            every { c.scoreRepo.findApprovedScoresByMemberIds(listOf(1L, 2L, 3L, 4L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(100, 75, 75, 50)

            When("백분위수 조회를 하면") {
                val result = c.service.execute()

                Then("나보다 낮은 점수를 가진 학생만 카운트된다") {
                    result.topPercentile shouldBe 25.0
                    result.bottomPercentile shouldBe 75.0
                }
            }
        }
    })
