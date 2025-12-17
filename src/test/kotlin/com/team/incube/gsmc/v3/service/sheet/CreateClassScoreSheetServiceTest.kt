package com.team.incube.gsmc.v3.service.sheet

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.sheet.service.impl.CreateClassScoreSheetServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.data.domain.PageImpl

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

        test("담임이 자신의 학급 점수표를 다운로드하면 성공한다") {
            val memberRepo = mockk<MemberExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            val service =
                CreateClassScoreSheetServiceImpl(
                    memberRepo,
                    scoreRepo,
                    currentMemberProvider,
                )

            val requester =
                Member(
                    id = 100L,
                    name = "담임교사",
                    email = "teacher@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = null,
                    role = MemberRole.HOMEROOM_TEACHER,
                )

            every { currentMemberProvider.getCurrentMember() } returns requester

            val students =
                listOf(
                    Member(1L, "학생1", "s1@test.com", 1, 1, 1, MemberRole.STUDENT),
                    Member(2L, "학생2", "s2@test.com", 1, 1, 2, MemberRole.STUDENT),
                )

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
            } returns PageImpl(students)

            every {
                scoreRepo.findByMemberIdsAndStatus(any(), ScoreStatus.APPROVED)
            } returns emptyList()

            val response = service.execute(1, 1)

            response.statusCode.value() shouldBe 200
            response.body shouldNotBe null
        }

        test("담임이 다른 학급 점수표에 접근하면 예외가 발생한다") {
            val memberRepo = mockk<MemberExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()

            val service =
                CreateClassScoreSheetServiceImpl(
                    memberRepo,
                    scoreRepo,
                    currentMemberProvider,
                )

            val requester =
                Member(
                    id = 100L,
                    name = "담임교사",
                    email = "teacher@test.com",
                    grade = 1,
                    classNumber = 2,
                    number = null,
                    role = MemberRole.HOMEROOM_TEACHER,
                )

            every { currentMemberProvider.getCurrentMember() } returns requester

            shouldThrow<GsmcException> {
                service.execute(1, 1)
            }.errorCode shouldBe ErrorCode.NOT_ASSIGNED_HOMEROOM_CLASS
        }
    })
