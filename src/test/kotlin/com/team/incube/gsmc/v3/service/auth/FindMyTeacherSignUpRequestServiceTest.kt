package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.auth.entity.TeacherSignUpRequestRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.impl.FindMyTeacherSignUpRequestServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.Optional

class FindMyTeacherSignUpRequestServiceTest :
    FunSpec({
        data class TestContext(
            val currentMemberProvider: CurrentMemberProvider,
            val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
            val service: FindMyTeacherSignUpRequestServiceImpl,
        )

        fun ctx(): TestContext {
            val currentMemberProvider = mockk<CurrentMemberProvider>()
            val teacherSignUpRequestRedisRepository = mockk<TeacherSignUpRequestRedisRepository>()
            val service = FindMyTeacherSignUpRequestServiceImpl(currentMemberProvider, teacherSignUpRequestRedisRepository)
            return TestContext(currentMemberProvider, teacherSignUpRequestRedisRepository, service)
        }

        fun teacherRequest(
            memberId: Long = 1L,
            name: String = "김선생",
            email: String = "teacher@gsm.hs.kr",
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
            memberId: Long = 2L,
            name: String = "김담임",
            email: String = "homeroom@gsm.hs.kr",
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

        test("내 선생님 권한 요청이 존재할 때 요청 정보를 반환한다") {
            val c = ctx()
            val memberId = 1L
            val request = teacherRequest(memberId = memberId, name = "김선생")

            every { c.currentMemberProvider.getCurrentMemberId() } returns memberId
            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.of(request)

            val result = c.service.execute()

            result.memberId shouldBe memberId
            result.name shouldBe "김선생"
            result.email shouldBe "teacher@gsm.hs.kr"
            result.requestedRole shouldBe MemberRole.TEACHER
            result.grade shouldBe null
            result.classNumber shouldBe null
        }

        test("내 담임선생님 권한 요청이 존재할 때 학년/반 정보를 포함하여 반환한다") {
            val c = ctx()
            val memberId = 2L
            val request = homeroomTeacherRequest(memberId = memberId, name = "김담임", grade = 2, classNumber = 3)

            every { c.currentMemberProvider.getCurrentMemberId() } returns memberId
            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.of(request)

            val result = c.service.execute()

            result.memberId shouldBe memberId
            result.name shouldBe "김담임"
            result.email shouldBe "homeroom@gsm.hs.kr"
            result.requestedRole shouldBe MemberRole.HOMEROOM_TEACHER
            result.grade shouldBe 2
            result.classNumber shouldBe 3
        }

        test("내 회원가입 요청이 존재하지 않을 때 TEACHER_SIGNUP_REQUEST_NOT_FOUND 예외가 발생한다") {
            val c = ctx()
            val memberId = 999L

            every { c.currentMemberProvider.getCurrentMemberId() } returns memberId
            every { c.teacherSignUpRequestRedisRepository.findById(memberId) } returns Optional.empty()

            val ex = shouldThrow<GsmcException> { c.service.execute() }
            ex.errorCode shouldBe ErrorCode.TEACHER_SIGNUP_REQUEST_NOT_FOUND
        }
    })
