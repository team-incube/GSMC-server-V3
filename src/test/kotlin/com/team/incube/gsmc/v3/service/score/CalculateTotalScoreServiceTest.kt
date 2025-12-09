package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.calculator.CategoryScoreCalculator
import com.team.incube.gsmc.v3.domain.score.calculator.ScoreCalculatorFactory
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.CalculateTotalScoreServiceImpl
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class CalculateTotalScoreServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: CalculateTotalScoreServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
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

            val service = CalculateTotalScoreServiceImpl(scoreRepo, currentMemberProvider)
            return TestData(scoreRepo, currentMemberProvider, service)
        }

        beforeTest {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                secondArg<Transaction.() -> Any>().invoke(mockk(relaxed = true))
            }

            mockkObject(ScoreCalculatorFactory)
        }

        afterTest {
            unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            unmockkAll()
        }

        Given("모든 점수를 포함하여 총점을 계산할 때") {
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
            val scores =
                listOf(
                    Score(
                        id = 1L,
                        member = member,
                        categoryType = CategoryType.CERTIFICATE,
                        status = ScoreStatus.APPROVED,
                        sourceId = 100L,
                        activityName = "자격증1",
                        scoreValue = 2.0,
                        rejectionReason = null,
                    ),
                    Score(
                        id = 2L,
                        member = member,
                        categoryType = CategoryType.AWARD,
                        status = ScoreStatus.APPROVED,
                        sourceId = 101L,
                        activityName = "수상1",
                        scoreValue = 1.0,
                        rejectionReason = null,
                    ),
                    Score(
                        id = 3L,
                        member = member,
                        categoryType = CategoryType.CERTIFICATE,
                        status = ScoreStatus.PENDING,
                        sourceId = 102L,
                        activityName = "자격증2",
                        scoreValue = 2.0,
                        rejectionReason = null,
                    ),
                )

            every { c.scoreRepo.findAllByMemberId(0L) } returns scores

            When("includeApprovedOnly가 false일 때") {
                // CERTIFICATE 계산기 모킹 - 모든 점수 포함
                val certificateCalculator = mockk<CategoryScoreCalculator>()
                every {
                    certificateCalculator.calculate(
                        match { it.size == 2 && it.all { score -> score.categoryType == CategoryType.CERTIFICATE } },
                        CategoryType.CERTIFICATE,
                        false,
                    )
                } returns 4 // 자격증1(2점) + 자격증2(2점)

                // AWARD 계산기 모킹
                val awardCalculator = mockk<CategoryScoreCalculator>()
                every {
                    awardCalculator.calculate(
                        match { it.size == 1 && it[0].categoryType == CategoryType.AWARD },
                        CategoryType.AWARD,
                        false,
                    )
                } returns 1 // 수상1(1점)

                every { ScoreCalculatorFactory.getCalculator(CategoryType.CERTIFICATE) } returns certificateCalculator
                every { ScoreCalculatorFactory.getCalculator(CategoryType.AWARD) } returns awardCalculator

                val res = c.service.execute(includeApprovedOnly = false)

                Then("모든 점수가 합산된다") {
                    res.totalScore shouldBe 5 // 4 + 1
                }
            }

            When("includeApprovedOnly가 true일 때") {
                // CERTIFICATE 계산기 모킹 - 승인된 점수만 계산
                val certificateCalculator = mockk<CategoryScoreCalculator>()
                every {
                    certificateCalculator.calculate(
                        match { it.size == 2 && it.all { score -> score.categoryType == CategoryType.CERTIFICATE } },
                        CategoryType.CERTIFICATE,
                        true,
                    )
                } returns 2 // 자격증1(2점)만 (자격증2는 PENDING이므로 제외)

                // AWARD 계산기 모킹
                val awardCalculator = mockk<CategoryScoreCalculator>()
                every {
                    awardCalculator.calculate(
                        match { it.size == 1 && it[0].categoryType == CategoryType.AWARD },
                        CategoryType.AWARD,
                        true,
                    )
                } returns 1 // 수상1(1점)

                every { ScoreCalculatorFactory.getCalculator(CategoryType.CERTIFICATE) } returns certificateCalculator
                every { ScoreCalculatorFactory.getCalculator(CategoryType.AWARD) } returns awardCalculator

                val res = c.service.execute(includeApprovedOnly = true)

                Then("승인된 점수만 합산된다") {
                    res.totalScore shouldBe 3 // 2 + 1
                }
            }
        }

        Given("점수가 없을 때") {
            val c = ctx()

            every { c.scoreRepo.findAllByMemberId(0L) } returns emptyList()

            When("execute를 호출하면") {
                val res = c.service.execute(includeApprovedOnly = false)

                Then("총점이 0이다") {
                    res.totalScore shouldBe 0
                }
            }
        }

        Given("외국어 점수가 여러 개 있을 때") {
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
            val scores =
                listOf(
                    Score(
                        id = 1L,
                        member = member,
                        categoryType = CategoryType.TOEIC,
                        status = ScoreStatus.APPROVED,
                        sourceId = 100L,
                        activityName = "TOEIC",
                        scoreValue = 800.0,
                        rejectionReason = null,
                    ),
                    Score(
                        id = 2L,
                        member = member,
                        categoryType = CategoryType.JLPT,
                        status = ScoreStatus.APPROVED,
                        sourceId = 101L,
                        activityName = "JLPT N2",
                        scoreValue = 2.0,
                        rejectionReason = null,
                    ),
                )

            every { c.scoreRepo.findAllByMemberId(0L) } returns scores

            When("execute를 호출하면") {
                // TOEIC 계산기 모킹 - 8점 반환
                val toeicCalculator = mockk<CategoryScoreCalculator>()
                every {
                    toeicCalculator.calculate(
                        match { it.any { score -> score.categoryType == CategoryType.TOEIC } },
                        CategoryType.TOEIC,
                        true,
                    )
                } returns 8

                // JLPT 계산기 모킹 - 8점 반환
                val jlptCalculator = mockk<CategoryScoreCalculator>()
                every {
                    jlptCalculator.calculate(
                        match { it.any { score -> score.categoryType == CategoryType.JLPT } },
                        CategoryType.JLPT,
                        true,
                    )
                } returns 8

                every { ScoreCalculatorFactory.getCalculator(CategoryType.TOEIC) } returns toeicCalculator
                every { ScoreCalculatorFactory.getCalculator(CategoryType.JLPT) } returns jlptCalculator

                val res = c.service.execute(includeApprovedOnly = true)

                Then("외국어 점수 중 최댓값이 선택된다") {
                    // TOEIC: 8점, JLPT: 8점 → max(8, 8) = 8점
                    res.totalScore shouldBe 8
                }
            }
        }

        Given("다양한 카테고리의 점수가 있을 때") {
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
            val scores =
                listOf(
                    Score(
                        id = 1L,
                        member = member,
                        categoryType = CategoryType.CERTIFICATE,
                        status = ScoreStatus.APPROVED,
                        sourceId = 100L,
                        activityName = "자격증1",
                        scoreValue = 2.0,
                        rejectionReason = null,
                    ),
                    Score(
                        id = 2L,
                        member = member,
                        categoryType = CategoryType.CERTIFICATE,
                        status = ScoreStatus.APPROVED,
                        sourceId = 101L,
                        activityName = "자격증2",
                        scoreValue = 2.0,
                        rejectionReason = null,
                    ),
                    Score(
                        id = 3L,
                        member = member,
                        categoryType = CategoryType.AWARD,
                        status = ScoreStatus.APPROVED,
                        sourceId = 102L,
                        activityName = "수상1",
                        scoreValue = 1.0,
                        rejectionReason = null,
                    ),
                    Score(
                        id = 4L,
                        member = member,
                        categoryType = CategoryType.EXTERNAL_ACTIVITY,
                        status = ScoreStatus.APPROVED,
                        sourceId = 103L,
                        activityName = "외부활동1",
                        scoreValue = 1.0,
                        rejectionReason = null,
                    ),
                )

            every { c.scoreRepo.findAllByMemberId(0L) } returns scores

            When("execute를 호출하면") {
                // CERTIFICATE 계산기 모킹 - 4점 반환
                val certificateCalculator = mockk<CategoryScoreCalculator>()
                every {
                    certificateCalculator.calculate(
                        match { it.size == 2 && it.all { score -> score.categoryType == CategoryType.CERTIFICATE } },
                        CategoryType.CERTIFICATE,
                        true,
                    )
                } returns 4 // 자격증1(2점) + 자격증2(2점)

                // AWARD 계산기 모킹 - 1점 반환
                val awardCalculator = mockk<CategoryScoreCalculator>()
                every {
                    awardCalculator.calculate(
                        match { it.size == 1 && it[0].categoryType == CategoryType.AWARD },
                        CategoryType.AWARD,
                        true,
                    )
                } returns 1

                // EXTERNAL_ACTIVITY 계산기 모킹 - 1점 반환
                val externalActivityCalculator = mockk<CategoryScoreCalculator>()
                every {
                    externalActivityCalculator.calculate(
                        match { it.size == 1 && it[0].categoryType == CategoryType.EXTERNAL_ACTIVITY },
                        CategoryType.EXTERNAL_ACTIVITY,
                        true,
                    )
                } returns 1

                every { ScoreCalculatorFactory.getCalculator(CategoryType.CERTIFICATE) } returns certificateCalculator
                every { ScoreCalculatorFactory.getCalculator(CategoryType.AWARD) } returns awardCalculator
                every { ScoreCalculatorFactory.getCalculator(CategoryType.EXTERNAL_ACTIVITY) } returns externalActivityCalculator

                val res = c.service.execute(includeApprovedOnly = true)

                Then("모든 카테고리의 점수가 합산된다") {
                    res.totalScore shouldBe 6 // 4 + 1 + 1
                }
            }
        }
    })
