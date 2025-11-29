package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.FindMyScoresServiceImpl
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class FindMyScoresServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: FindMyScoresServiceImpl,
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

            val service = FindMyScoresServiceImpl(scoreRepo, currentMemberProvider)
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

        Given("내 점수 목록을 조회할 때") {
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
                        categoryType = CategoryType.AWARD,
                        status = ScoreStatus.PENDING,
                        sourceId = 100L,
                        activityName = "수상1",
                        scoreValue = 10.0,
                        rejectionReason = null,
                    ),
                    Score(
                        id = 2L,
                        member = member,
                        categoryType = CategoryType.CERTIFICATE,
                        status = ScoreStatus.APPROVED,
                        sourceId = 101L,
                        activityName = "자격증1",
                        scoreValue = 15.0,
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
                val res = c.service.execute(categoryType = null, status = null)

                Then("내 점수 목록이 반환된다") {
                    res.scores.size shouldBe 2
                    res.scores[0].scoreId shouldBe 1L
                    res.scores[0].activityName shouldBe "수상1"
                    res.scores[0].scoreValue shouldBe 10.0
                    res.scores[0].scoreStatus shouldBe ScoreStatus.PENDING
                    res.scores[0].categoryNames.koreanName shouldBe CategoryType.AWARD.koreanName
                    res.scores[1].scoreId shouldBe 2L
                    res.scores[1].activityName shouldBe "자격증1"
                    res.scores[1].scoreValue shouldBe 15.0
                    res.scores[1].scoreStatus shouldBe ScoreStatus.APPROVED
                }

                Then("저장소 메서드가 호출된다") {
                    verify(exactly = 1) {
                        c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                            memberId = 0L,
                            categoryType = null,
                            status = null,
                        )
                    }
                }
            }
        }

        Given("특정 categoryType으로 필터링할 때") {
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
                        categoryType = CategoryType.AWARD,
                        status = ScoreStatus.PENDING,
                        sourceId = 100L,
                        activityName = "수상1",
                        scoreValue = 10.0,
                        rejectionReason = null,
                    ),
                )

            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = 0L,
                    categoryType = CategoryType.AWARD,
                    status = null,
                )
            } returns scores

            When("execute를 호출하면") {
                val res = c.service.execute(categoryType = CategoryType.AWARD, status = null)

                Then("필터링된 점수 목록이 반환된다") {
                    res.scores.size shouldBe 1
                    res.scores[0].categoryNames.koreanName shouldBe CategoryType.AWARD.koreanName
                }
            }
        }

        Given("특정 status로 필터링할 때") {
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
                        id = 2L,
                        member = member,
                        categoryType = CategoryType.CERTIFICATE,
                        status = ScoreStatus.APPROVED,
                        sourceId = 101L,
                        activityName = "자격증1",
                        scoreValue = 15.0,
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
                val res = c.service.execute(categoryType = null, status = ScoreStatus.APPROVED)

                Then("필터링된 점수 목록이 반환된다") {
                    res.scores.size shouldBe 1
                    res.scores[0].scoreStatus shouldBe ScoreStatus.APPROVED
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
                val res = c.service.execute(categoryType = null, status = null)

                Then("빈 목록이 반환된다") {
                    res.scores.size shouldBe 0
                }
            }
        }
    })
