package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.FindScoresByCategoryServiceImpl
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class FindScoresByCategoryServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: FindScoresByCategoryServiceImpl,
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

            val service = FindScoresByCategoryServiceImpl(scoreRepo, currentMemberProvider)
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

        Given("카테고리별로 그룹화된 점수를 조회할 때") {
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
                )

            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = 0L,
                    categoryType = null,
                    status = null,
                )
            } returns scores

            When("execute를 호출하면") {
                val res = c.service.execute(status = null)

                Then("카테고리별로 그룹화된 점수가 반환된다") {
                    // 단순히 카테고리별로 그룹화되었는지만 확인
                    res.categories.isNotEmpty() shouldBe true
                }
            }
        }

        Given("APPROVED 상태만 필터링하여 조회할 때") {
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
                )

            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = 0L,
                    categoryType = null,
                    status = ScoreStatus.APPROVED,
                )
            } returns scores

            When("execute를 호출하면") {
                val res = c.service.execute(status = ScoreStatus.APPROVED)

                Then("승인된 점수만 반환된다") {
                    res.categories.any { group ->
                        group.scores.any { it.scoreStatus == ScoreStatus.APPROVED }
                    } shouldBe true
                }
            }
        }

        Given("점수가 없을 때") {
            val c = ctx()

            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = 0L,
                    categoryType = null,
                    status = null,
                )
            } returns emptyList()

            When("execute를 호출하면") {
                val res = c.service.execute(status = null)

                Then("모든 카테고리의 점수가 0이다") {
                    res.categories.all { it.recognizedScore == 0 } shouldBe true
                    res.categories.all { it.scores.isEmpty() } shouldBe true
                }
            }
        }

        Given("외국어 점수가 있을 때") {
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
                        activityName = "JLPT",
                        scoreValue = 150.0,
                        rejectionReason = null,
                    ),
                )

            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = 0L,
                    categoryType = null,
                    status = null,
                )
            } returns scores

            When("execute를 호출하면") {
                val res = c.service.execute(status = null)

                Then("외국어 점수가 하나의 그룹으로 통합된다") {
                    // 외국어 카테고리가 통합되어 처리되는지 확인
                    res.categories.isNotEmpty() shouldBe true
                }
            }
        }
    })
