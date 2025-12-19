package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.auth.entity.TeacherSignUpRequestRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.impl.RejectTeacherSignUpRequestServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import java.time.Instant
import java.util.Optional

class RejectTeacherSignUpRequestServiceTest :
    BehaviorSpec({
        data class TestContext(
            val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
            val memberExposedRepository: MemberExposedRepository,
            val alertExposedRepository: AlertExposedRepository,
            val service: RejectTeacherSignUpRequestServiceImpl,
        )

        fun ctx(): TestContext {
            val teacherSignUpRequestRedisRepository = mockk<TeacherSignUpRequestRedisRepository>()
            val memberExposedRepository = mockk<MemberExposedRepository>()
            val alertExposedRepository = mockk<AlertExposedRepository>()
            val service = RejectTeacherSignUpRequestServiceImpl(teacherSignUpRequestRedisRepository, memberExposedRepository, alertExposedRepository)
            return TestContext(teacherSignUpRequestRedisRepository, memberExposedRepository, alertExposedRepository, service)
        }

        fun unauthorizedMember(
            id: Long = 1L,
            email: String = "test@gsm.hs.kr",
        ) = Member(
            id = id,
            name = "",
            email = email,
            grade = null,
            classNumber = null,
            number = null,
            role = MemberRole.UNAUTHORIZED,
        )

        fun teacherRequest(
            memberId: Long = 1L,
            name: String = "김선생",
            email: String = "test@gsm.hs.kr",
        ) = TeacherSignUpRequestRedisEntity(
            memberId = memberId,
            name = name,
            email = email,
            requestedRole = MemberRole.TEACHER,
            grade = null,
            classNumber = null,
            requestedAt = Instant.now(),
        )

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

        Given("선생님 권한 요청이 존재할 때") {
            val c = ctx()
            val memberId = 1L
            val email = "test@gsm.hs.kr"
            val member = unauthorizedMember(id = memberId, email = email)
            val request = teacherRequest(memberId = memberId, email = email)

            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.of(request)
            every { c.teacherSignUpRequestRedisRepository.deleteById(memberId) } returns Unit
            every { c.memberExposedRepository.findById(memberId) } returns member
            every { c.memberExposedRepository.deleteMemberByEmail(email) } returns 1
            every { c.alertExposedRepository.deleteAllBySenderId(memberId) } returns 0
            every { c.alertExposedRepository.deleteAllByReceiverId(memberId) } returns 0

            When("execute를 호출하면") {
                c.service.execute(memberId)

                Then("발신자로 참조된 알림이 삭제된다") {
                    verify(exactly = 1) { c.alertExposedRepository.deleteAllBySenderId(memberId) }
                }

                Then("수신자로 참조된 알림이 삭제된다") {
                    verify(exactly = 1) { c.alertExposedRepository.deleteAllByReceiverId(memberId) }
                }

                Then("DB에서 회원이 삭제된다") {
                    verify(exactly = 1) { c.memberExposedRepository.deleteMemberByEmail(email) }
                }

                Then("Redis에서 요청이 삭제된다") {
                    verify(exactly = 1) { c.teacherSignUpRequestRedisRepository.deleteById(memberId) }
                }
            }
        }

        Given("Redis에 요청이 존재하지 않을 때") {
            val c = ctx()
            val memberId = 999L

            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.empty()

            When("execute를 호출하면") {
                Then("TEACHER_SIGNUP_REQUEST_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(memberId) }
                    ex.errorCode shouldBe ErrorCode.TEACHER_SIGNUP_REQUEST_NOT_FOUND
                    verify(exactly = 0) { c.teacherSignUpRequestRedisRepository.deleteById(any()) }
                    verify(exactly = 0) { c.memberExposedRepository.deleteMemberByEmail(any()) }
                }
            }
        }

        Given("회원이 DB에 존재하지 않을 때") {
            val c = ctx()
            val memberId = 1L
            val request = teacherRequest(memberId = memberId)

            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.of(request)
            every { c.memberExposedRepository.findById(memberId) } returns null

            When("execute를 호출하면") {
                Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(memberId) }
                    ex.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                    verify(exactly = 0) { c.teacherSignUpRequestRedisRepository.deleteById(memberId) }
                    verify(exactly = 0) { c.memberExposedRepository.deleteMemberByEmail(any()) }
                }
            }
        }
    })
