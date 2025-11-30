package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.CreateEvidenceDraftRequest
import com.team.incube.gsmc.v3.domain.evidence.service.impl.CreateEvidenceDraftServiceImpl
import com.team.incube.gsmc.v3.domain.file.dto.File
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
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class CreateEvidenceDraftServiceTest :
    BehaviorSpec({
        data class Ctx(
            val currentMemberProvider: CurrentMemberProvider,
            val fileExposedRepository: FileExposedRepository,
            val service: CreateEvidenceDraftServiceImpl,
        )

        fun ctx(): Ctx {
            val c = mockk<CurrentMemberProvider>()
            val f = mockk<FileExposedRepository>()
            val s = CreateEvidenceDraftServiceImpl(c, f)
            return Ctx(c, f, s)
        }

        beforeTest {
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                secondArg<Transaction.() -> Any>().invoke(mockk(relaxed = true))
            }
        }

        afterTest {
            unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        }

        Given("증빙자료 임시저장 요청이 주어지면") {
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

            val request =
                CreateEvidenceDraftRequest(
                    title = "대회 참가 증빙",
                    content = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.",
                    fileIds = listOf(1L, 2L),
                )

            val mockFiles =
                listOf(
                    File(
                        id = 1L,
                        member = 1L,
                        originalName = "file1.pdf",
                        storeName = "stored_file1.pdf",
                        uri = "https://example.com/file1.pdf",
                    ),
                    File(
                        id = 2L,
                        member = 1L,
                        originalName = "file2.pdf",
                        storeName = "stored_file2.pdf",
                        uri = "https://example.com/file2.pdf",
                    ),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.fileExposedRepository.findAllByIdIn(listOf(1L, 2L)) } returns mockFiles

            When("임시저장을 생성하면") {
                val result = c.service.execute(request)

                Then("요청한 데이터가 응답으로 반환된다") {
                    result.title shouldBe "대회 참가 증빙"
                    result.content shouldBe "2024년 전국 프로그래밍 대회 참가 증빙자료입니다."
                    result.files.size shouldBe 2
                }
            }
        }

        Given("빈 데이터로 증빙자료 임시저장 요청이 주어지면") {
            val c = ctx()
            val member =
                Member(
                    id = 2L,
                    name = "김철수",
                    email = "kim@gsm.hs.kr",
                    grade = 1,
                    classNumber = 2,
                    number = 3,
                    role = MemberRole.STUDENT,
                )

            val request =
                CreateEvidenceDraftRequest(
                    title = "",
                    content = "",
                    fileIds = emptyList(),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member

            When("임시저장을 생성하면") {
                val result = c.service.execute(request)

                Then("빈 데이터가 응답으로 반환된다") {
                    result.title shouldBe ""
                    result.content shouldBe ""
                    result.files shouldBe emptyList()
                }
            }
        }

        Given("일부 필드만 입력된 증빙자료 임시저장 요청이 주어지면") {
            val c = ctx()
            val member =
                Member(
                    id = 3L,
                    name = "이영희",
                    email = "lee@gsm.hs.kr",
                    grade = 3,
                    classNumber = 1,
                    number = 10,
                    role = MemberRole.STUDENT,
                )

            val request =
                CreateEvidenceDraftRequest(
                    title = "임시 제목",
                    content = "",
                    fileIds = listOf(1L),
                )

            val mockFiles =
                listOf(
                    File(
                        id = 1L,
                        member = 3L,
                        originalName = "file1.pdf",
                        storeName = "stored_file1.pdf",
                        uri = "https://example.com/file1.pdf",
                    ),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.fileExposedRepository.findAllByIdIn(listOf(1L)) } returns mockFiles

            When("임시저장을 생성하면") {
                val result = c.service.execute(request)

                Then("입력된 필드만 포함된 응답이 반환된다") {
                    result.title shouldBe "임시 제목"
                    result.content shouldBe ""
                    result.files.size shouldBe 1
                }
            }
        }
    })
