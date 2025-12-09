package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.FindScoresByCategoryByMemberIdServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class FindScoresByCategoryByMemberIdServiceTest :
    BehaviorSpec({
        data class TestData(
            val scoreRepo: ScoreExposedRepository,
            val memberRepo: MemberExposedRepository,
            val service: FindScoresByCategoryByMemberIdServiceImpl,
        )

        fun ctx(): TestData {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val memberRepo = mockk<MemberExposedRepository>()
            val service = FindScoresByCategoryByMemberIdServiceImpl(scoreRepo, memberRepo)
            return TestData(scoreRepo, memberRepo, service)
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

        Given("특정 멤버의 카테고리별 점수를 조회할 때") {
            val c = ctx()
            val memberId = 1L
            val member =
                Member(
                    id = memberId,
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
                )

            every { c.memberRepo.existsById(memberId) } returns true
            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = memberId,
                    categoryType = null,
                    status = null,
                )
            } returns scores

            When("execute를 호출하면") {
                val res = c.service.execute(memberId, status = null)

                Then("해당 멤버의 카테고리별 점수가 반환된다") {
                    res.categories.isNotEmpty() shouldBe true
                }
            }
        }

        Given("존재하지 않는 멤버 ID로 조회할 때") {
            val c = ctx()
            val memberId = 999L

            every { c.memberRepo.existsById(memberId) } returns false

            When("execute를 호출하면") {
                Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(memberId, null) }
                    ex.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                }
            }
        }

        Given("특정 상태로 필터링하여 조회할 때") {
            val c = ctx()
            val memberId = 1L
            val member =
                Member(
                    id = memberId,
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

            every { c.memberRepo.existsById(memberId) } returns true
            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = memberId,
                    categoryType = null,
                    status = ScoreStatus.APPROVED,
                )
            } returns scores

            When("execute를 호출하면") {
                val res = c.service.execute(memberId, status = ScoreStatus.APPROVED)

                Then("필터링된 점수가 반환된다") {
                    res.categories.any { group ->
                        group.scores.any { it.scoreStatus == ScoreStatus.APPROVED }
                    } shouldBe true
                }
            }
        }

        Given("점수가 없는 멤버를 조회할 때") {
            val c = ctx()
            val memberId = 1L

            every { c.memberRepo.existsById(memberId) } returns true
            every {
                c.scoreRepo.findByMemberIdAndCategoryTypeAndStatus(
                    memberId = memberId,
                    categoryType = null,
                    status = null,
                )
            } returns emptyList()

            When("execute를 호출하면") {
                val res = c.service.execute(memberId, status = null)

                Then("모든 카테고리의 점수가 0이다") {
                    res.categories.all { it.recognizedScore == 0 } shouldBe true
                    res.categories.all { it.scores.isEmpty() } shouldBe true
                }
            }
        }
    })
