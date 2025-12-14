package com.team.incube.gsmc.v3.service.sheet

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.sheet.service.impl.CreateClassScoreSheetServiceImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class CreateClassScoreSheetServiceTest :
    FunSpec({
        // 스펙 초기화 시점에 transaction mock 설정
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

        test("반별 성적표가 정상적으로 생성된다") {
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

            val res = service.execute(1, 1)

            res shouldNotBe null
        }
    })
