package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.auth.entity.TeacherSignUpRequestRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.impl.ApproveTeacherSignUpRequestServiceImpl
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

class ApproveTeacherSignUpRequestServiceTest :
    BehaviorSpec({
        data class TestContext(
            val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
            val memberExposedRepository: MemberExposedRepository,
            val service: ApproveTeacherSignUpRequestServiceImpl,
        )

        fun ctx(): TestContext {
            val teacherSignUpRequestRedisRepository = mockk<TeacherSignUpRequestRedisRepository>()
            val memberExposedRepository = mockk<MemberExposedRepository>()
            val service = ApproveTeacherSignUpRequestServiceImpl(teacherSignUpRequestRedisRepository, memberExposedRepository)
            return TestContext(teacherSignUpRequestRedisRepository, memberExposedRepository, service)
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

        fun homeroomTeacherRequest(
            memberId: Long = 1L,
            name: String = "김담임",
            email: String = "test@gsm.hs.kr",
            grade: Int = 2,
            classNumber: Int = 3,
        ) = TeacherSignUpRequestRedisEntity(
            memberId = memberId,
            name = name,
            email = email,
            requestedRole = MemberRole.HOMEROOM_TEACHER,
            grade = grade,
            classNumber = classNumber,
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
            val member = unauthorizedMember(id = memberId)
            val request = teacherRequest(memberId = memberId)

            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.of(request)
            every { c.memberExposedRepository.findById(memberId) } returns member
            every {
                c.memberExposedRepository.update(
                    id = memberId,
                    name = request.name,
                    email = member.email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.TEACHER,
                )
            } returns 1
            every { c.teacherSignUpRequestRedisRepository.deleteById(memberId) } returns Unit

            When("execute를 호출하면") {
                c.service.execute(memberId)

                Then("회원 정보가 TEACHER 권한으로 업데이트된다") {
                    verify(exactly = 1) {
                        c.memberExposedRepository.update(
                            id = memberId,
                            name = request.name,
                            email = member.email,
                            grade = null,
                            classNumber = null,
                            number = null,
                            role = MemberRole.TEACHER,
                        )
                    }
                }

                Then("Redis에서 요청이 삭제된다") {
                    verify(exactly = 1) { c.teacherSignUpRequestRedisRepository.deleteById(memberId) }
                }
            }
        }

        Given("담임선생님 권한 요청이 존재할 때") {
            val c = ctx()
            val memberId = 2L
            val member = unauthorizedMember(id = memberId)
            val request = homeroomTeacherRequest(memberId = memberId, grade = 2, classNumber = 3)

            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.of(request)
            every { c.memberExposedRepository.findById(memberId) } returns member
            every {
                c.memberExposedRepository.update(
                    id = memberId,
                    name = request.name,
                    email = member.email,
                    grade = 2,
                    classNumber = 3,
                    number = null,
                    role = MemberRole.HOMEROOM_TEACHER,
                )
            } returns 1
            every { c.teacherSignUpRequestRedisRepository.deleteById(memberId) } returns Unit

            When("execute를 호출하면") {
                c.service.execute(memberId)

                Then("회원 정보가 HOMEROOM_TEACHER 권한으로 학년/반 포함하여 업데이트된다") {
                    verify(exactly = 1) {
                        c.memberExposedRepository.update(
                            id = memberId,
                            name = request.name,
                            email = member.email,
                            grade = 2,
                            classNumber = 3,
                            number = null,
                            role = MemberRole.HOMEROOM_TEACHER,
                        )
                    }
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
                    verify(exactly = 0) { c.memberExposedRepository.update(any(), any(), any(), any(), any(), any(), any()) }
                    verify(exactly = 0) { c.teacherSignUpRequestRedisRepository.deleteById(any()) }
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
                    verify(exactly = 0) { c.memberExposedRepository.update(any(), any(), any(), any(), any(), any(), any()) }
                }
            }
        }
    })
