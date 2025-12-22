package com.team.incube.gsmc.v3.service.alert

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.impl.CreateAlertServiceImpl
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class CreateAlertServiceTest :
    FunSpec({
        data class TestContext(
            val memberRepo: MemberExposedRepository,
            val alertRepo: AlertExposedRepository,
            val scoreRepo: ScoreExposedRepository,
            val service: CreateAlertServiceImpl,
        )

        fun ctx(): TestContext {
            val memberRepo = mockk<MemberExposedRepository>()
            val alertRepo = mockk<AlertExposedRepository>()
            val scoreRepo = mockk<ScoreExposedRepository>()
            val service = CreateAlertServiceImpl(memberRepo, alertRepo, scoreRepo)
            return TestContext(memberRepo, alertRepo, scoreRepo, service)
        }

        fun teacher(
            id: Long = 1L,
            name: String = "담임",
        ) = Member(
            id = id,
            name = name,
            email = "$name@school.com",
            grade = 0,
            classNumber = 0,
            number = 0,
            role = MemberRole.TEACHER,
        )

        fun student(
            id: Long = 2L,
            name: String = "학생",
        ) = Member(
            id = id,
            name = name,
            email = "$name@school.com",
            grade = 1,
            classNumber = 1,
            number = 1,
            role = MemberRole.STUDENT,
        )

        fun pendingScore(
            id: Long = 10L,
            owner: Member = student(),
        ) = Score(
            id = id,
            member = owner,
            categoryType = CategoryType.CERTIFICATE,
            status = ScoreStatus.PENDING,
            sourceId = null,
            activityName = "정보처리기능사",
            scoreValue = 5.0,
            rejectionReason = null,
            updatedAt = null,
        )

        // 스펙 초기화 시점에 transaction mock 설정 (beforeTest보다 먼저 실행)
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

        test("승인 알림을 생성할 때 알림 저장 시 올바른 콘텐츠가 사용된다") {
            val c = ctx()
            val sender = teacher()
            val receiver = student()
            val score = pendingScore(owner = receiver)

            every { c.memberRepo.findById(sender.id) } returns sender
            every { c.memberRepo.findById(receiver.id) } returns receiver
            every { c.scoreRepo.findById(score.id!!) } returns score
            every { c.alertRepo.save(any(), any(), any(), any(), any()) } returns mockk()

            c.service.execute(sender.id, receiver.id, score.id!!, AlertType.APPROVED)

            verify(exactly = 1) {
                c.alertRepo.save(
                    sender,
                    receiver,
                    score,
                    AlertType.APPROVED,
                    "${score.categoryType.koreanName} 점수를 ${sender.name} 선생님께서 통과시키셨습니다.",
                )
            }
        }

        test("거부 알림을 생성할 때 알림 저장 시 올바른 콘텐츠가 사용된다") {
            val c = ctx()
            val sender = teacher()
            val receiver = student()
            val score = pendingScore(owner = receiver)

            every { c.memberRepo.findById(sender.id) } returns sender
            every { c.memberRepo.findById(receiver.id) } returns receiver
            every { c.scoreRepo.findById(score.id!!) } returns score
            every { c.alertRepo.save(any(), any(), any(), any(), any()) } returns mockk()

            c.service.execute(sender.id, receiver.id, score.id!!, AlertType.REJECTED)

            verify(exactly = 1) {
                c.alertRepo.save(
                    sender,
                    receiver,
                    score,
                    AlertType.REJECTED,
                    "${score.categoryType.koreanName} 점수를 ${sender.name} 선생님께서 거부하셨습니다.",
                )
            }
        }

        test("점수 등록 알림을 생성할 때 알림 저장 시 올바른 콘텐츠가 사용된다") {
            val c = ctx()
            val sender = student()
            val receiver = teacher()
            val score = pendingScore(owner = sender)

            every { c.memberRepo.findById(sender.id) } returns sender
            every { c.memberRepo.findById(receiver.id) } returns receiver
            every { c.scoreRepo.findById(score.id!!) } returns score
            every { c.alertRepo.save(any(), any(), any(), any(), any()) } returns mockk()

            c.service.execute(sender.id, receiver.id, score.id!!, AlertType.ADD_SCORE)

            verify(exactly = 1) {
                c.alertRepo.save(
                    sender,
                    receiver,
                    score,
                    AlertType.ADD_SCORE,
                    "${score.categoryType.koreanName} 점수를 ${sender.name} 학생이 등록하였습니다.",
                )
            }
        }

        test("sender가 존재하지 않으면 MEMBER_NOT_FOUND 예외가 발생한다") {
            val c = ctx()

            every { c.memberRepo.findById(999L) } returns null

            val exception =
                shouldThrow<GsmcException> {
                    c.service.execute(999L, 2L, 1L, AlertType.REJECTED)
                }
            exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            verify(exactly = 0) { c.alertRepo.save(any(), any(), any(), any(), any()) }
        }

        test("receiver가 존재하지 않으면 MEMBER_NOT_FOUND 예외가 발생한다") {
            val c = ctx()

            every { c.memberRepo.findById(1L) } returns teacher()
            every { c.memberRepo.findById(999L) } returns null

            val exception =
                shouldThrow<GsmcException> {
                    c.service.execute(1L, 999L, 1L, AlertType.REJECTED)
                }
            exception.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
            verify(exactly = 0) { c.alertRepo.save(any(), any(), any(), any(), any()) }
        }

        test("scoreId가 존재하지 않으면 SCORE_NOT_FOUND 예외가 발생한다") {
            val c = ctx()

            every { c.memberRepo.findById(1L) } returns teacher()
            every { c.memberRepo.findById(2L) } returns student()
            every { c.scoreRepo.findById(999L) } returns null

            val exception =
                shouldThrow<GsmcException> {
                    c.service.execute(1L, 2L, 999L, AlertType.ADD_SCORE)
                }
            exception.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
            verify(exactly = 0) { c.alertRepo.save(any(), any(), any(), any(), any()) }
        }
    })
