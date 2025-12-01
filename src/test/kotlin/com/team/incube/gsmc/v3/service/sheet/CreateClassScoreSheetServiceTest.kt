package com.team.incube.gsmc.v3.service.sheet

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.sheet.service.impl.CreateClassScoreSheetServiceImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class CreateClassScoreSheetServiceTest :
    BehaviorSpec({
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

        Given("반별 성적표 생성") {
            val memberRepo = mockk<MemberExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val service = CreateClassScoreSheetServiceImpl(memberRepo, scoreRepo)

            val students =
                listOf(
                    Member(1L, "학생1", "s1@test.com", 1, 1, 1, MemberRole.STUDENT),
                    Member(2L, "학생2", "s2@test.com", 1, 1, 2, MemberRole.STUDENT),
                )
            val page = PageImpl(students, PageRequest.of(0, 1000), 2)

            every {
                memberRepo.searchMembers(
                    email = null,
                    name = null,
                    role = MemberRole.STUDENT,
                    grade = 1,
                    classNumber = 1,
                    number = null,
                    sortBy = any(),
                    pageable = any(),
                )
            } returns page
            every { scoreRepo.findByMemberIdsAndStatus(any(), ScoreStatus.APPROVED) } returns emptyList()

            When("1학년 1반의 성적표를 생성하면") {
                val res = service.execute(1, 1)

                Then("성적표가 생성된다") {
                    res shouldNotBe null
                }
            }
        }
    })
