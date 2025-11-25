package com.team.incube.gsmc.v3.service.file

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.impl.FindMyFilesServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class FindMyFilesServiceTest :
    BehaviorSpec({

        data class TestData(
            val mockFileRepository: FileExposedRepository,
            val mockCurrentMemberProvider: CurrentMemberProvider,
            val findMyFilesService: FindMyFilesServiceImpl,
        )

        fun createTestContext(): TestData {
            val mockFileRepository = mockk<FileExposedRepository>()
            val mockCurrentMemberProvider = mockk<CurrentMemberProvider>()

            every { mockCurrentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 1L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val findMyFilesService = FindMyFilesServiceImpl(mockFileRepository, mockCurrentMemberProvider)

            return TestData(
                mockFileRepository = mockFileRepository,
                mockCurrentMemberProvider = mockCurrentMemberProvider,
                findMyFilesService = findMyFilesService,
            )
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

        Given("현재 로그인한 사용자가 여러 개의 파일을 소유하고 있을 때") {
            val context = createTestContext()
            val userId = 1L
            val mockFiles =
                listOf(
                    File(
                        fileId = 1L,
                        memberId = userId,
                        fileOriginalName = "document1.pdf",
                        fileStoredName = "20251125120000_abc123.pdf",
                        fileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/file1.pdf",
                    ),
                    File(
                        fileId = 2L,
                        memberId = userId,
                        fileOriginalName = "image1.jpg",
                        fileStoredName = "20251125120001_def456.jpg",
                        fileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/file2.jpg",
                    ),
                    File(
                        fileId = 3L,
                        memberId = userId,
                        fileOriginalName = "spreadsheet1.xlsx",
                        fileStoredName = "20251125120002_ghi789.xlsx",
                        fileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/file3.xlsx",
                    ),
                )

            every { context.mockFileRepository.findAllByUserId(userId) } returns mockFiles

            When("내 파일 목록 조회를 실행하면") {
                val result = context.findMyFilesService.execute()

                Then("현재 사용자 정보를 조회해야 한다") {
                    verify(exactly = 1) { context.mockCurrentMemberProvider.getCurrentMember() }
                }

                Then("사용자 ID로 파일 목록을 조회해야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findAllByUserId(userId) }
                }

                Then("모든 파일이 반환되어야 한다") {
                    result.files shouldHaveSize 3
                }

                Then("반환된 파일들이 올바른 정보를 포함해야 한다") {
                    result.files[0].fileId shouldBe 1L
                    result.files[0].originalName shouldBe "document1.pdf"
                    result.files[0].storedName shouldBe "20251125120000_abc123.pdf"
                    result.files[0].uri shouldBe "https://gsmc-bucket.s3.amazonaws.com/evidences/file1.pdf"

                    result.files[1].fileId shouldBe 2L
                    result.files[1].originalName shouldBe "image1.jpg"
                    result.files[1].storedName shouldBe "20251125120001_def456.jpg"
                    result.files[1].uri shouldBe "https://gsmc-bucket.s3.amazonaws.com/evidences/file2.jpg"

                    result.files[2].fileId shouldBe 3L
                    result.files[2].originalName shouldBe "spreadsheet1.xlsx"
                    result.files[2].storedName shouldBe "20251125120002_ghi789.xlsx"
                    result.files[2].uri shouldBe "https://gsmc-bucket.s3.amazonaws.com/evidences/file3.xlsx"
                }
            }
        }

        Given("현재 로그인한 사용자가 파일을 소유하지 않았을 때") {
            val context = createTestContext()
            val userId = 1L

            every { context.mockFileRepository.findAllByUserId(userId) } returns emptyList()

            When("내 파일 목록 조회를 실행하면") {
                val result = context.findMyFilesService.execute()

                Then("빈 파일 목록이 반환되어야 한다") {
                    result.files shouldHaveSize 0
                }

                Then("파일 저장소를 조회해야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findAllByUserId(userId) }
                }
            }
        }

        Given("현재 로그인한 사용자가 단일 파일만 소유하고 있을 때") {
            val context = createTestContext()
            val userId = 1L
            val mockFiles =
                listOf(
                    File(
                        fileId = 1L,
                        memberId = userId,
                        fileOriginalName = "single-file.pdf",
                        fileStoredName = "20251125120000_single.pdf",
                        fileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/single.pdf",
                    ),
                )

            every { context.mockFileRepository.findAllByUserId(userId) } returns mockFiles

            When("내 파일 목록 조회를 실행하면") {
                val result = context.findMyFilesService.execute()

                Then("단일 파일이 반환되어야 한다") {
                    result.files shouldHaveSize 1
                    result.files[0].fileId shouldBe 1L
                    result.files[0].originalName shouldBe "single-file.pdf"
                }
            }
        }
    })
