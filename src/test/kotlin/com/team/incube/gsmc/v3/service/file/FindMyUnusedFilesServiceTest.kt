package com.team.incube.gsmc.v3.service.file

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.impl.FindMyUnusedFilesServiceImpl
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

class FindMyUnusedFilesServiceTest :
    BehaviorSpec({

        data class TestData(
            val mockFileRepository: FileExposedRepository,
            val mockCurrentMemberProvider: CurrentMemberProvider,
            val findMyUnusedFilesService: FindMyUnusedFilesServiceImpl,
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

            val findMyUnusedFilesService = FindMyUnusedFilesServiceImpl(mockFileRepository, mockCurrentMemberProvider)

            return TestData(
                mockFileRepository = mockFileRepository,
                mockCurrentMemberProvider = mockCurrentMemberProvider,
                findMyUnusedFilesService = findMyUnusedFilesService,
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

        Given("현재 로그인한 사용자가 사용하지 않은 파일들을 소유하고 있을 때") {
            val context = createTestContext()
            val userId = 1L
            val mockUnusedFiles =
                listOf(
                    File(
                        id = 1L,
                        member = userId,
                        originalName = "unused-document.pdf",
                        storeName = "20251125120000_unused1.pdf",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/unused1.pdf",
                    ),
                    File(
                        id = 2L,
                        member = userId,
                        originalName = "unused-image.jpg",
                        storeName = "20251125120001_unused2.jpg",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/unused2.jpg",
                    ),
                )

            every { context.mockFileRepository.findUnusedFilesByUserId(userId) } returns mockUnusedFiles

            When("미사용 파일 목록 조회를 실행하면") {
                val result = context.findMyUnusedFilesService.execute()

                Then("현재 사용자 정보를 조회해야 한다") {
                    verify(exactly = 1) { context.mockCurrentMemberProvider.getCurrentMember() }
                }

                Then("사용자 ID로 미사용 파일 목록을 조회해야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findUnusedFilesByUserId(userId) }
                }

                Then("모든 미사용 파일이 반환되어야 한다") {
                    result.files shouldHaveSize 2
                }

                Then("반환된 파일들이 올바른 정보를 포함해야 한다") {
                    val expectedFileItems =
                        mockUnusedFiles.map { file ->
                            com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem(
                                id = file.id,
                                member = file.member,
                                originalName = file.originalName,
                                storeName = file.storeName,
                                uri = file.uri,
                            )
                        }
                    result.files shouldBe expectedFileItems
                }
            }
        }

        Given("현재 로그인한 사용자가 미사용 파일을 가지고 있지 않을 때") {
            val context = createTestContext()
            val userId = 1L

            every { context.mockFileRepository.findUnusedFilesByUserId(userId) } returns emptyList()

            When("미사용 파일 목록 조회를 실행하면") {
                val result = context.findMyUnusedFilesService.execute()

                Then("빈 파일 목록이 반환되어야 한다") {
                    result.files shouldHaveSize 0
                }

                Then("파일 저장소를 조회해야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findUnusedFilesByUserId(userId) }
                }
            }
        }

        Given("현재 로그인한 사용자가 단일 미사용 파일만 소유하고 있을 때") {
            val context = createTestContext()
            val userId = 1L
            val mockUnusedFiles =
                listOf(
                    File(
                        id = 5L,
                        member = userId,
                        originalName = "single-unused.hwp",
                        storeName = "20251125120000_single_unused.hwp",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/single_unused.hwp",
                    ),
                )

            every { context.mockFileRepository.findUnusedFilesByUserId(userId) } returns mockUnusedFiles

            When("미사용 파일 목록 조회를 실행하면") {
                val result = context.findMyUnusedFilesService.execute()

                Then("단일 미사용 파일이 반환되어야 한다") {
                    val expectedFileItems =
                        mockUnusedFiles.map { file ->
                            com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem(
                                id = file.id,
                                member = file.member,
                                originalName = file.originalName,
                                storeName = file.storeName,
                                uri = file.uri,
                            )
                        }
                    result.files shouldBe expectedFileItems
                }
            }
        }

        Given("여러 종류의 파일 확장자를 가진 미사용 파일들이 있을 때") {
            val context = createTestContext()
            val userId = 1L
            val mockUnusedFiles =
                listOf(
                    File(
                        id = 10L,
                        member = userId,
                        originalName = "document.pdf",
                        storeName = "20251125120000_doc.pdf",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/doc.pdf",
                    ),
                    File(
                        id = 11L,
                        member = userId,
                        originalName = "image.png",
                        storeName = "20251125120001_img.png",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/img.png",
                    ),
                    File(
                        id = 12L,
                        member = userId,
                        originalName = "sheet.xlsx",
                        storeName = "20251125120002_sheet.xlsx",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/sheet.xlsx",
                    ),
                    File(
                        id = 13L,
                        member = userId,
                        originalName = "presentation.pptx",
                        storeName = "20251125120003_ppt.pptx",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/ppt.pptx",
                    ),
                )

            every { context.mockFileRepository.findUnusedFilesByUserId(userId) } returns mockUnusedFiles

            When("미사용 파일 목록 조회를 실행하면") {
                val result = context.findMyUnusedFilesService.execute()

                Then("모든 확장자의 파일들이 올바르게 반환되어야 한다") {
                    val expectedFileItems =
                        mockUnusedFiles.map { file ->
                            com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem(
                                id = file.id,
                                member = file.member,
                                originalName = file.originalName,
                                storeName = file.storeName,
                                uri = file.uri,
                            )
                        }
                    result.files shouldBe expectedFileItems
                }
            }
        }
    })
