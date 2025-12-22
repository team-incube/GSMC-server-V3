package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.auth.entity.TeacherSignUpRequestRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.TeacherSignUpRequestRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.impl.FindTeacherSignUpRequestsServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant

class FindTeacherSignUpRequestsServiceTest :
    FunSpec({
        data class TestContext(
            val teacherSignUpRequestRedisRepository: TeacherSignUpRequestRedisRepository,
            val service: FindTeacherSignUpRequestsServiceImpl,
        )

        fun ctx(): TestContext {
            val teacherSignUpRequestRedisRepository = mockk<TeacherSignUpRequestRedisRepository>()
            val service = FindTeacherSignUpRequestsServiceImpl(teacherSignUpRequestRedisRepository)
            return TestContext(teacherSignUpRequestRedisRepository, service)
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

        test("요청 목록이 비어있을 때 빈 리스트를 반환한다") {
            val c = ctx()

            every { c.teacherSignUpRequestRedisRepository.findAll() } returns emptyList()

            val result = c.service.execute()

            result.shouldBeEmpty()
        }

        test("선생님 권한 요청이 1개 있을 때 해당 요청을 반환한다") {
            val c = ctx()
            val request = teacherRequest()

            every { c.teacherSignUpRequestRedisRepository.findAll() } returns listOf(request)

            val result = c.service.execute()

            result shouldHaveSize 1
            result[0].memberId shouldBe request.memberId
            result[0].name shouldBe request.name
            result[0].email shouldBe request.email
            result[0].requestedRole shouldBe MemberRole.TEACHER
            result[0].grade shouldBe null
            result[0].classNumber shouldBe null
        }

        test("담임선생님 권한 요청이 1개 있을 때 학년/반 정보를 포함하여 반환한다") {
            val c = ctx()
            val request = homeroomTeacherRequest(grade = 2, classNumber = 3)

            every { c.teacherSignUpRequestRedisRepository.findAll() } returns listOf(request)

            val result = c.service.execute()

            result shouldHaveSize 1
            result[0].memberId shouldBe request.memberId
            result[0].name shouldBe request.name
            result[0].requestedRole shouldBe MemberRole.HOMEROOM_TEACHER
            result[0].grade shouldBe 2
            result[0].classNumber shouldBe 3
        }

        test("여러 요청이 있을 때 전체 리스트를 반환한다") {
            val c = ctx()
            val request1 = teacherRequest(memberId = 1L, name = "선생님1")
            val request2 = homeroomTeacherRequest(memberId = 2L, name = "담임1", grade = 1, classNumber = 1)
            val request3 = teacherRequest(memberId = 3L, name = "선생님2")

            every { c.teacherSignUpRequestRedisRepository.findAll() } returns listOf(request1, request2, request3)

            val result = c.service.execute()

            result shouldHaveSize 3
            result[0].memberId shouldBe 1L
            result[0].name shouldBe "선생님1"
            result[0].requestedRole shouldBe MemberRole.TEACHER

            result[1].memberId shouldBe 2L
            result[1].name shouldBe "담임1"
            result[1].requestedRole shouldBe MemberRole.HOMEROOM_TEACHER
            result[1].grade shouldBe 1
            result[1].classNumber shouldBe 1

            result[2].memberId shouldBe 3L
            result[2].name shouldBe "선생님2"
            result[2].requestedRole shouldBe MemberRole.TEACHER
        }
    })
