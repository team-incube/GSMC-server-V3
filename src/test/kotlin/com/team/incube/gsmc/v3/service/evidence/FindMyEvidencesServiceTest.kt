package com.team.incube.gsmc.v3.service.evidence

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.impl.FindMyEvidencesServiceImpl
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class FindMyEvidencesServiceTest :
    BehaviorSpec({
        data class Ctx(
            val evidenceRepo: EvidenceExposedRepository,
            val currentMemberProvider: CurrentMemberProvider,
            val service: FindMyEvidencesServiceImpl,
        )

        fun ctx(): Ctx {
            val e = mockk<EvidenceExposedRepository>()
            val c = mockk<CurrentMemberProvider>()
            val s = FindMyEvidencesServiceImpl(e, c)
            return Ctx(e, c, s)
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

        Given("사용자의 증빙자료 목록 조회 요청이 주어지면") {
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

            val now = LocalDateTime.of(2025, 11, 19, 10, 0)
            val files1 = listOf(File(id = 1L, memberId = 1L, originalName = "file1.pdf", storeName = "stored1.pdf", uri = "uri1"))
            val files2 = listOf(File(id = 2L, memberId = 1L, originalName = "file2.pdf", storeName = "stored2.pdf", uri = "uri2"))

            val evidences =
                listOf(
                    Evidence(
                        id = 10L,
                        memberId = 1L,
                        title = "대회 참가 증빙",
                        content = "2024년 전국 프로그래밍 대회 참가",
                        createdAt = now,
                        updatedAt = now,
                        files = files1,
                    ),
                    Evidence(
                        id = 20L,
                        memberId = 1L,
                        title = "봉사활동 증빙",
                        content = "지역 봉사활동 참여",
                        createdAt = now.plusDays(1),
                        updatedAt = now.plusDays(1),
                        files = files2,
                    ),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.evidenceRepo.findAllByMemberId(1L) } returns evidences

            When("내 증빙자료를 조회하면") {
                val result = c.service.execute()

                Then("내가 작성한 모든 증빙자료가 반환된다") {
                    result.evidences shouldHaveSize 2
                    result.evidences[0].evidenceId shouldBe 10L
                    result.evidences[0].title shouldBe "대회 참가 증빙"
                    result.evidences[0].content shouldBe "2024년 전국 프로그래밍 대회 참가"
                    result.evidences[0].files shouldBe listOf(GetFileResponse(1L, 1L, "file1.pdf", "stored1.pdf", "uri1"))

                    result.evidences[1].evidenceId shouldBe 20L
                    result.evidences[1].title shouldBe "봉사활동 증빙"
                    result.evidences[1].content shouldBe "지역 봉사활동 참여"
                    result.evidences[1].files shouldBe listOf(GetFileResponse(2L, 1L, "file2.pdf", "stored2.pdf", "uri2"))
                }

                Then("현재 사용자 정보를 조회한다") {
                    verify { c.currentMemberProvider.getCurrentMember() }
                }

                Then("사용자의 증빙자료 목록을 조회한다") {
                    verify { c.evidenceRepo.findAllByMemberId(1L) }
                }
            }
        }

        Given("증빙자료가 없는 사용자의 조회 요청이 주어지면") {
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

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.evidenceRepo.findAllByMemberId(2L) } returns emptyList()

            When("내 증빙자료를 조회하면") {
                val result = c.service.execute()

                Then("빈 목록이 반환된다") {
                    result.evidences.shouldBeEmpty()
                }

                Then("현재 사용자 정보를 조회한다") {
                    verify { c.currentMemberProvider.getCurrentMember() }
                }

                Then("사용자의 증빙자료 목록을 조회한다") {
                    verify { c.evidenceRepo.findAllByMemberId(2L) }
                }
            }
        }

        Given("증빙자료가 하나만 있는 사용자의 조회 요청이 주어지면") {
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

            val now = LocalDateTime.of(2025, 11, 15, 14, 30)
            val files = listOf(File(id = 3L, memberId = 3L, originalName = "doc.pdf", storeName = "stored_doc.pdf", uri = "uri-doc"))

            val evidences =
                listOf(
                    Evidence(
                        id = 30L,
                        memberId = 3L,
                        title = "프로젝트 결과물",
                        content = "팀 프로젝트 최종 결과물입니다.",
                        createdAt = now,
                        updatedAt = now,
                        files = files,
                    ),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.evidenceRepo.findAllByMemberId(3L) } returns evidences

            When("내 증빙자료를 조회하면") {
                val result = c.service.execute()

                Then("하나의 증빙자료가 반환된다") {
                    result.evidences shouldHaveSize 1
                    result.evidences[0].evidenceId shouldBe 30L
                    result.evidences[0].title shouldBe "프로젝트 결과물"
                    result.evidences[0].content shouldBe "팀 프로젝트 최종 결과물입니다."
                    result.evidences[0].createdAt shouldBe now
                    result.evidences[0].updatedAt shouldBe now
                    result.evidences[0].files shouldBe listOf(GetFileResponse(3L, 3L, "doc.pdf", "stored_doc.pdf", "uri-doc"))
                }

                Then("리포지토리 조회가 정확히 한 번 수행된다") {
                    verify(exactly = 1) { c.evidenceRepo.findAllByMemberId(3L) }
                }
            }
        }

        Given("파일이 없는 증빙자료를 가진 사용자의 조회 요청이 주어지면") {
            val c = ctx()
            val member =
                Member(
                    id = 4L,
                    name = "박민수",
                    email = "park@gsm.hs.kr",
                    grade = 2,
                    classNumber = 3,
                    number = 7,
                    role = MemberRole.STUDENT,
                )

            val now = LocalDateTime.of(2025, 10, 1, 9, 0)

            val evidences =
                listOf(
                    Evidence(
                        id = 40L,
                        memberId = 4L,
                        title = "활동 일지",
                        content = "일일 활동 내용 기록",
                        createdAt = now,
                        updatedAt = now,
                        files = emptyList(),
                    ),
                )

            every { c.currentMemberProvider.getCurrentMember() } returns member
            every { c.evidenceRepo.findAllByMemberId(4L) } returns evidences

            When("내 증빙자료를 조회하면") {
                val result = c.service.execute()

                Then("파일이 없는 증빙자료가 반환된다") {
                    result.evidences shouldHaveSize 1
                    result.evidences[0].evidenceId shouldBe 40L
                    result.evidences[0].title shouldBe "활동 일지"
                    result.evidences[0].files.shouldBeEmpty()
                }
            }
        }
    })
