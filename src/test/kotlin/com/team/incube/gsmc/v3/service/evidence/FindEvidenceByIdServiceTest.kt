package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.impl.FindEvidenceByIdServiceImpl
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class FindEvidenceByIdServiceTest :
    BehaviorSpec({
        data class Ctx(
            val repo: EvidenceExposedRepository,
            val service: FindEvidenceByIdServiceImpl,
        )

        fun ctx(): Ctx {
            val r = mockk<EvidenceExposedRepository>()
            val s = FindEvidenceByIdServiceImpl(r)
            return Ctx(r, s)
        }

        beforeTest {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                secondArg<Transaction.() -> Any>().invoke(mockk(relaxed = true))
            }
        }
        afterTest { unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt") }

        Given("존재하는 증빙 ID가 주어졌을 때") {
            val c = ctx()
            val id = 1L
            val now = LocalDateTime.of(2025, 10, 1, 12, 0)
            val files = listOf(File(10, 0L, "a.pdf", "sa.pdf", "uri-a"))
            val evidence = Evidence(id, 0L, "title", "content", now, now, files)
            every { c.repo.findById(id) } returns evidence

            When("execute를 호출하면") {
                val res: GetEvidenceResponse = c.service.execute(id)

                Then("증빙이 정상적으로 반환된다") {
                    res shouldNotBe null
                    res.evidenceId shouldBe id
                    res.title shouldBe "title"
                    res.content shouldBe "content"
                    res.createdAt shouldBe now
                    res.updatedAt shouldBe now
                    res.files shouldBe listOf(FileItem(10, "a.pdf", "sa.pdf", "uri-a"))
                }

                Then("리포지토리에서 1회 조회된다") {
                    verify(exactly = 1) { c.repo.findById(id) }
                }
            }
        }

        Given("존재하지 않는 증빙 ID가 주어졌을 때") {
            val c = ctx()
            val id = 999L
            every { c.repo.findById(id) } returns null

            When("execute를 호출하면") {
                Then("EVIDENCE_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> { c.service.execute(id) }
                    ex.errorCode shouldBe ErrorCode.EVIDENCE_NOT_FOUND
                }
            }
        }
    })
