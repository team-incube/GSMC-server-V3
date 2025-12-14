package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceDraftRedisRepository
import com.team.incube.gsmc.v3.domain.evidence.service.impl.FindMyEvidenceDraftServiceImpl
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

class FindEvidenceDraftServiceTest :
    BehaviorSpec({
        data class Ctx(
            val currentMemberProvider: CurrentMemberProvider,
            val evidenceDraftRedisRepository: EvidenceDraftRedisRepository,
            val fileExposedRepository: FileExposedRepository,
            val service: FindMyEvidenceDraftServiceImpl,
        )

        fun ctx(): Ctx {
            val c = mockk<CurrentMemberProvider>()
            val e = mockk<EvidenceDraftRedisRepository>(relaxed = true)
            val f = mockk<FileExposedRepository>(relaxed = true)
            val s = FindMyEvidenceDraftServiceImpl(c, e, f)
            return Ctx(c, e, f, s)
        }

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

        afterSpec { unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt") }

        Given("증빙자료 임시저장 조회 요청이 주어지면") {
            val c = ctx()
            val member =
                Member(
                    id = 1L,
                    name = "홍길동",
                    email = "test@gsm.hs.kr",
                    grade = 2,
                    classNumber = 1,
                    number = 5,
                    role = MemberRole.STUDENT,
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.currentMemberProvider.getCurrentMemberId() } returns member.id
            every { c.evidenceDraftRedisRepository.findById(member.id) } returns java.util.Optional.empty()

            When("execute를 호출하면") {
                val result = c.service.execute()

                Then("null이 반환된다 (캐시에 저장된 데이터가 없는 경우)") {
                    result shouldBe null
                }
            }
        }
    })
