package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.auth.entity.TeacherSignUpRequestRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.impl.CreateTeacherSignUpRequestServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class CreateTeacherSignUpRequestServiceTest :
    BehaviorSpec({
        data class TestContext(
            val currentMemberProvider: CurrentMemberProvider,
            val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
            val memberExposedRepository: MemberExposedRepository,
            val alertExposedRepository: AlertExposedRepository,
            val service: CreateTeacherSignUpRequestServiceImpl,
        )

        fun ctx(): TestContext {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val teacherSignUpRequestRedisRepository = mockk<TeacherSignUpRequestRedisRepository>()
            val memberExposedRepository = mockk<MemberExposedRepository>()
            val alertExposedRepository = mockk<AlertExposedRepository>()
            val service =
                CreateTeacherSignUpRequestServiceImpl(
                    currentMemberProvider,
                    teacherSignUpRequestRedisRepository,
                    memberExposedRepository,
                    alertExposedRepository,
                )
            return TestContext(
                currentMemberProvider,
                teacherSignUpRequestRedisRepository,
                memberExposedRepository,
                alertExposedRepository,
                service,
            )
        }

        fun unauthorizedMember(
            id: Long = 1L,
            email: String = "unauthorized@gsm.hs.kr",
        ) = Member(
            id = id,
            name = "",
            email = email,
            grade = null,
            classNumber = null,
            number = null,
            role = MemberRole.UNAUTHORIZED,
        )

        fun teacher(
            id: Long = 10L,
            name: String = "선생님",
        ) = Member(
            id = id,
            name = name,
            email = "${name}@gsm.hs.kr",
            grade = null,
            classNumber = null,
            number = null,
            role = MemberRole.TEACHER,
        )

        fun homeroomTeacher(
            id: Long = 20L,
            name: String = "담임",
        ) = Member(
            id = id,
            name = name,
            email = "${name}@gsm.hs.kr",
            grade = 1,
            classNumber = 1,
            number = null,
            role = MemberRole.HOMEROOM_TEACHER,
        )

        fun rootMember(
            id: Long = 100L,
            name: String = "관리자",
        ) = Member(
            id = id,
            name = name,
            email = "${name}@gsm.hs.kr",
            grade = null,
            classNumber = null,
            number = null,
            role = MemberRole.ROOT,
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

        Given("UNAUTHORIZED 사용자가 선생님 권한을 요청할 때") {
            val c = ctx()
            val currentMember = unauthorizedMember()
            val name = "김선생"
            val requestedRole = MemberRole.TEACHER
            val targetMembers = listOf(teacher(id = 10L), homeroomTeacher(id = 20L), rootMember(id = 100L))

            every { c.currentMemberProvider.getCurrentMember() } returns currentMember
            every { c.teacherSignUpRequestRedisRepository.save(any()) } returns mockk()
            every {
                c.memberExposedRepository.findAllByRoleIn(
                    listOf(MemberRole.TEACHER, MemberRole.HOMEROOM_TEACHER, MemberRole.ROOT),
                )
            } returns targetMembers
            every { c.alertExposedRepository.saveWithoutScore(any(), any(), any(), any()) } returns mockk()

            When("execute를 호출하면") {
                c.service.execute(name, requestedRole, null, null)

                Then("Redis에 요청이 저장된다") {
                    val slot = slot<TeacherSignUpRequestRedisEntity>()
                    verify(exactly = 1) { c.teacherSignUpRequestRedisRepository.save(capture(slot)) }

                    val savedRequest = slot.captured
                    savedRequest.memberId shouldBe currentMember.id
                    savedRequest.name shouldBe name
                    savedRequest.email shouldBe currentMember.email
                    savedRequest.requestedRole shouldBe MemberRole.TEACHER
                    savedRequest.grade shouldBe null
                    savedRequest.classNumber shouldBe null
                }

                Then("TEACHER, HOMEROOM_TEACHER, ROOT 권한자 전원에게 알림이 발송된다") {
                    verify(exactly = 3) {
                        c.alertExposedRepository.saveWithoutScore(
                            sender = currentMember,
                            receiver = any(),
                            alertType = AlertType.TEACHER_SIGNUP_REQUEST,
                            content = "${name}님이 TEACHER 권한 회원가입을 요청했습니다.",
                        )
                    }
                }
            }
        }

        Given("UNAUTHORIZED 사용자가 담임선생님 권한을 요청할 때 (학년/반 포함)") {
            val c = ctx()
            val currentMember = unauthorizedMember()
            val name = "김담임"
            val requestedRole = MemberRole.HOMEROOM_TEACHER
            val grade = 2
            val classNumber = 3
            val targetMembers = listOf(teacher(id = 10L))

            every { c.currentMemberProvider.getCurrentMember() } returns currentMember
            every { c.teacherSignUpRequestRedisRepository.save(any()) } returns mockk()
            every {
                c.memberExposedRepository.findAllByRoleIn(
                    listOf(MemberRole.TEACHER, MemberRole.HOMEROOM_TEACHER, MemberRole.ROOT),
                )
            } returns targetMembers
            every { c.alertExposedRepository.saveWithoutScore(any(), any(), any(), any()) } returns mockk()

            When("execute를 호출하면") {
                c.service.execute(name, requestedRole, grade, classNumber)

                Then("Redis에 학년/반 정보가 포함된 요청이 저장된다") {
                    val slot = slot<TeacherSignUpRequestRedisEntity>()
                    verify(exactly = 1) { c.teacherSignUpRequestRedisRepository.save(capture(slot)) }

                    val savedRequest = slot.captured
                    savedRequest.requestedRole shouldBe MemberRole.HOMEROOM_TEACHER
                    savedRequest.grade shouldBe grade
                    savedRequest.classNumber shouldBe classNumber
                }

                Then("알림이 발송된다") {
                    verify(exactly = 1) {
                        c.alertExposedRepository.saveWithoutScore(
                            any(),
                            any(),
                            AlertType.TEACHER_SIGNUP_REQUEST,
                            "${name}님이 HOMEROOM_TEACHER 권한 회원가입을 요청했습니다.",
                        )
                    }
                }
            }
        }

        Given("잘못된 권한(STUDENT)을 요청할 때") {
            val c = ctx()
            val currentMember = unauthorizedMember()
            every { c.currentMemberProvider.getCurrentMember() } returns currentMember

            When("execute를 호출하면") {
                Then("INVALID_TEACHER_ROLE 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute("이름", MemberRole.STUDENT, null, null)
                        }
                    ex.errorCode shouldBe ErrorCode.INVALID_TEACHER_ROLE
                    verify(exactly = 0) { c.teacherSignUpRequestRedisRepository.save(any()) }
                }
            }
        }

        Given("담임선생님 권한을 요청하는데 학년이 누락되었을 때") {
            val c = ctx()
            val currentMember = unauthorizedMember()
            every { c.currentMemberProvider.getCurrentMember() } returns currentMember

            When("execute를 호출하면") {
                Then("HOMEROOM_TEACHER_GRADE_CLASS_REQUIRED 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute("이름", MemberRole.HOMEROOM_TEACHER, null, 1)
                        }
                    ex.errorCode shouldBe ErrorCode.HOMEROOM_TEACHER_GRADE_CLASS_REQUIRED
                    verify(exactly = 0) { c.teacherSignUpRequestRedisRepository.save(any()) }
                }
            }
        }

        Given("담임선생님 권한을 요청하는데 반이 누락되었을 때") {
            val c = ctx()
            val currentMember = unauthorizedMember()
            every { c.currentMemberProvider.getCurrentMember() } returns currentMember

            When("execute를 호출하면") {
                Then("HOMEROOM_TEACHER_GRADE_CLASS_REQUIRED 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute("이름", MemberRole.HOMEROOM_TEACHER, 1, null)
                        }
                    ex.errorCode shouldBe ErrorCode.HOMEROOM_TEACHER_GRADE_CLASS_REQUIRED
                    verify(exactly = 0) { c.teacherSignUpRequestRedisRepository.save(any()) }
                }
            }
        }
    })
