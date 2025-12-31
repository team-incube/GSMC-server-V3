package com.team.incube.gsmc.v3.service.score

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.impl.UpdateScoreStatusServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class UpdateScoreStatusServiceTest :
    FunSpec({
        data class Ctx(
            val scoreRepo: ScoreExposedRepository,
            val memberRepo: com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: UpdateScoreStatusServiceImpl,
        )

        fun ctx(currentMember: Member = Member(1L, "Teacher", "t@test.com", 0, 0, 0, MemberRole.TEACHER)): Ctx {
            val scoreRepo = mockk<ScoreExposedRepository>()
            val memberRepo = mockk<com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository>()
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            every { currentMemberProvider.getCurrentMember() } returns currentMember
            val service = UpdateScoreStatusServiceImpl(scoreRepo, memberRepo, currentMemberProvider)
            return Ctx(scoreRepo, memberRepo, currentMemberProvider, service)
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

        test("유효한 scoreId와 status로 상태 업데이트에 성공한다") {
            val c = ctx()
            val scoreId = 1L
            val status = ScoreStatus.APPROVED
            val score = mockk<Score>()
            val student = Member(2L, "Student", "s@test.com", 1, 1, 1, MemberRole.STUDENT)

            every { score.member } returns student
            every { c.scoreRepo.findById(scoreId) } returns score
            every { c.scoreRepo.updateStatusById(scoreId, status) } returns 1

            c.service.execute(scoreId, status)

            verify(exactly = 1) { c.scoreRepo.updateStatusById(scoreId, status) }
        }

        test("존재하지 않는 scoreId로 상태 업데이트를 시도하면 SCORE_NOT_FOUND 예외가 발생한다") {
            val c = ctx()
            val scoreId = 999L
            val status = ScoreStatus.REJECTED
            every { c.scoreRepo.findById(scoreId) } returns null

            val ex = shouldThrow<GsmcException> { c.service.execute(scoreId, status) }
            ex.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
        }

        test("모든 상태 값이 정확히 전달되어 업데이트된다") {
            val scoreIdBase = 1000L
            ScoreStatus.entries.forEachIndexed { idx, status ->
                val c = ctx()
                val scoreId = scoreIdBase + idx
                val score = mockk<Score>()
                val student = Member(2L, "Student", "s@test.com", 1, 1, 1, MemberRole.STUDENT)

                every { score.member } returns student
                every { c.scoreRepo.findById(scoreId) } returns score
                every { c.scoreRepo.updateStatusById(scoreId, status) } returns 1

                c.service.execute(scoreId, status)

                verify(exactly = 1) { c.scoreRepo.updateStatusById(scoreId, status) }
            }
        }
    })
