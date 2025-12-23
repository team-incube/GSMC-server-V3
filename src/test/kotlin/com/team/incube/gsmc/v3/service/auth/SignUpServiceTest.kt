package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.auth.service.impl.SignUpServiceImpl
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
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class SignUpServiceTest :
    BehaviorSpec({

        data class TestContext(
            val currentMemberProvider: CurrentMemberProvider,
            val memberExposedRepository: MemberExposedRepository,
            val service: SignUpServiceImpl,
        )

        fun ctx(): TestContext {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val memberExposedRepository = mockk<MemberExposedRepository>()

            val service =
                SignUpServiceImpl(
                    currentMemberProvider = currentMemberProvider,
                    memberExposedRepository = memberExposedRepository,
                )

            return TestContext(
                currentMemberProvider,
                memberExposedRepository,
                service,
            )
        }

        val mockTransaction = mockk<JdbcTransaction>(relaxed = true)

        mockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        every {
            org.jetbrains.exposed.v1.jdbc.transactions.transaction(
                db = null,
                statement = any<JdbcTransaction.() -> Any?>(),
            )
        } answers {
            @Suppress("UNCHECKED_CAST")
            val block = it.invocation.args.last() as JdbcTransaction.() -> Any?
            block.invoke(mockTransaction)
        }

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        Given("로그인된 사용자가 회원가입을 진행할 때") {
            val c = ctx()
            val memberId = 1L
            val email = "student@gsm.hs.kr"

            val member =
                Member(
                    id = memberId,
                    name = "기존이름",
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )

            val name = "홍길동"
            val studentNumber = 2323
            val expectedGrade = studentNumber / 1000
            val expectedClassNumber = (studentNumber / 100) % 10
            val expectedNumber = studentNumber % 100

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every {
                c.memberExposedRepository.existsByGradeAndClassNumberAndNumber(
                    grade = expectedGrade,
                    classNumber = expectedClassNumber,
                    number = expectedNumber,
                )
            } returns false
            every {
                c.memberExposedRepository.update(
                    id = memberId,
                    name = name,
                    email = email,
                    grade = expectedGrade,
                    classNumber = expectedClassNumber,
                    number = expectedNumber,
                    role = MemberRole.STUDENT,
                )
            } returns 1

            When("중복된 학번이 없을 때 execute를 호출하면") {
                c.service.execute(name, studentNumber)

                Then("회원 정보가 STUDENT 권한과 학번 정보로 업데이트된다") {
                    verify(exactly = 1) {
                        c.memberExposedRepository.existsByGradeAndClassNumberAndNumber(
                            grade = expectedGrade,
                            classNumber = expectedClassNumber,
                            number = expectedNumber,
                        )
                    }
                    verify(exactly = 1) {
                        c.memberExposedRepository.update(
                            id = memberId,
                            name = name,
                            email = email,
                            grade = expectedGrade,
                            classNumber = expectedClassNumber,
                            number = expectedNumber,
                            role = MemberRole.STUDENT,
                        )
                    }
                }
            }
        }

        Given("이미 사용 중인 학번으로 회원가입을 시도할 때") {
            val c = ctx()
            val memberId = 1L
            val email = "new@gsm.hs.kr"

            val member =
                Member(
                    id = memberId,
                    name = "기존이름",
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )

            val name = "홍길동"
            val studentNumber = 2323
            val expectedGrade = studentNumber / 1000
            val expectedClassNumber = (studentNumber / 100) % 10
            val expectedNumber = studentNumber % 100

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every {
                c.memberExposedRepository.existsByGradeAndClassNumberAndNumber(
                    grade = expectedGrade,
                    classNumber = expectedClassNumber,
                    number = expectedNumber,
                )
            } returns true

            When("execute를 호출하면") {
                val exception =
                    shouldThrow<GsmcException> {
                        c.service.execute(name, studentNumber)
                    }

                Then("MEMBER_STUDENT_NUMBER_ALREADY_EXISTS 예외가 발생한다") {
                    exception.errorCode shouldBe ErrorCode.MEMBER_STUDENT_NUMBER_ALREADY_EXISTS
                }
            }
        }
    })
