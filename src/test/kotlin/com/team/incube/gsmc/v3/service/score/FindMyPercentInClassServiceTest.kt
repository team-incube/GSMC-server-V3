package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.calculator.TotalScoreCalculator
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.FindMyPercentInClassServiceImpl
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

class FindMyPercentInClassServiceTest :
    BehaviorSpec({
        data class TestData(
            val currentMemberProvider: CurrentMemberProvider,
            val memberRepo: MemberExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val totalScoreCalculator: TotalScoreCalculator,
            val service: FindMyPercentInClassServiceImpl,
        )

        fun ctx(): TestData {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val memberRepo = mockk<MemberExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val totalScoreCalculator = mockk<TotalScoreCalculator>()

            val service =
                FindMyPercentInClassServiceImpl(
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

        Given("학년 또는 반 정보가 없는 학생인 경우") {
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
                            c.service.execute(includeApprovedOnly = true)
                        }
                    exception.errorCode shouldBe ErrorCode.MEMBER_GRADE_CLASS_NOT_SET
                }
            }
        }

        Given("학급에 학생이 1명만 있는 경우") {
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
            every { c.memberRepo.findStudentsByGradeAndClassNumber(1, 1) } returns listOf(currentMember)

            When("백분위수 조회를 하면") {
                val result = c.service.execute(includeApprovedOnly = true)

                Then("상위 100%, 하위 0%가 반환된다") {
                    result.topPercentile shouldBe 100.0
                    result.bottomPercentile shouldBe 0.0
                }
            }
        }

        Given("학급에 여러 학생이 있고 현재 학생이 중간 순위인 경우") {
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
                    classNumber = 1,
                    number = 3,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1, student2, student3)

            every { c.currentMemberProvider.getCurrentMember() } returns student2
            every { c.memberRepo.findStudentsByGradeAndClassNumber(1, 1) } returns students
            every { c.scoreRepo.findAllByMemberIds(listOf(1L, 2L, 3L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(50, 75, 90)

            When("백분위수 조회를 하면") {
                val result = c.service.execute(includeApprovedOnly = true)

                Then("상위 약 66.67%, 하위 약 33.33%가 반환된다") {
                    result.topPercentile shouldBe 66.66666666666667
                    result.bottomPercentile shouldBe 33.33333333333333
                }
            }
        }

        Given("학급에 여러 학생이 있고 현재 학생이 1등인 경우") {
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
                    classNumber = 1,
                    number = 3,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1, student2, student3)

            every { c.currentMemberProvider.getCurrentMember() } returns student1
            every { c.memberRepo.findStudentsByGradeAndClassNumber(1, 1) } returns students
            every { c.scoreRepo.findAllByMemberIds(listOf(1L, 2L, 3L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(100, 75, 50)

            When("백분위수 조회를 하면") {
                val result = c.service.execute(includeApprovedOnly = true)

                Then("상위 약 33.33%, 하위 약 66.67%가 반환된다") {
                    result.topPercentile shouldBe 33.33333333333334
                    result.bottomPercentile shouldBe 66.66666666666666
                }
            }
        }

        Given("학급에 여러 학생이 있고 현재 학생이 꼴등인 경우") {
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
                    classNumber = 1,
                    number = 3,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1, student2, student3)

            every { c.currentMemberProvider.getCurrentMember() } returns student3
            every { c.memberRepo.findStudentsByGradeAndClassNumber(1, 1) } returns students
            every { c.scoreRepo.findAllByMemberIds(listOf(1L, 2L, 3L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(100, 75, 50)

            When("백분위수 조회를 하면") {
                val result = c.service.execute(includeApprovedOnly = true)

                Then("상위 100%, 하위 0%가 반환된다") {
                    result.topPercentile shouldBe 100.0
                    result.bottomPercentile shouldBe 0.0
                }
            }
        }

        Given("학급에 동점자가 여러 명 있는 경우") {
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
                    classNumber = 1,
                    number = 3,
                    role = MemberRole.STUDENT,
                )

            val student4 =
                Member(
                    id = 4L,
                    name = "학생4",
                    email = "student4@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 4,
                    role = MemberRole.STUDENT,
                )

            val students = listOf(student1, student2, student3, student4)

            every { c.currentMemberProvider.getCurrentMember() } returns student2
            every { c.memberRepo.findStudentsByGradeAndClassNumber(1, 1) } returns students
            every { c.scoreRepo.findAllByMemberIds(listOf(1L, 2L, 3L, 4L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), true) } returnsMany listOf(100, 75, 75, 50)

            When("백분위수 조회를 하면") {
                val result = c.service.execute(includeApprovedOnly = true)

                Then("나보다 낮은 점수를 가진 학생만 카운트된다") {
                    result.topPercentile shouldBe 75.0
                    result.bottomPercentile shouldBe 25.0
                }
            }
        }

        Given("includeApprovedOnly가 false인 경우") {
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

            val students = listOf(student1, student2)

            every { c.currentMemberProvider.getCurrentMember() } returns student1
            every { c.memberRepo.findStudentsByGradeAndClassNumber(1, 1) } returns students
            every { c.scoreRepo.findAllByMemberIds(listOf(1L, 2L)) } returns emptyList()

            every { c.totalScoreCalculator.calculate(emptyList(), false) } returnsMany listOf(100, 80)

            When("백분위수 조회를 하면") {
                val result = c.service.execute(includeApprovedOnly = false)

                Then("PENDING 점수도 포함하여 계산된다") {
                    result.topPercentile shouldBe 50.0
                    result.bottomPercentile shouldBe 50.0
                }
            }
        }
    })
