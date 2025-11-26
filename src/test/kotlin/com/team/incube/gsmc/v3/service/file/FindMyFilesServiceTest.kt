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
                        id = 1L,
                        member = userId,
                        originalName = "document1.pdf",
                        storeName = "20251125120000_abc123.pdf",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/file1.pdf",
                    ),
                    File(
                        id = 2L,
                        member = userId,
                        originalName = "image1.jpg",
                        storeName = "20251125120001_def456.jpg",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/file2.jpg",
                    ),
                    File(
                        id = 3L,
                        member = userId,
                        originalName = "spreadsheet1.xlsx",
                        storeName = "20251125120002_ghi789.xlsx",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/file3.xlsx",
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
                    val expectedFileItems =
                        mockFiles.map { file ->
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
                        id = 1L,
                        member = userId,
                        originalName = "single-file.pdf",
                        storeName = "20251125120000_single.pdf",
                        uri = "https://gsmc-bucket.s3.amazonaws.com/evidences/single.pdf",
                    ),
                )

            every { context.mockFileRepository.findAllByUserId(userId) } returns mockFiles

            When("내 파일 목록 조회를 실행하면") {
                val result = context.findMyFilesService.execute()

                Then("단일 파일이 반환되어야 한다") {
                    val expectedFileItems =
                        mockFiles.map { file ->
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
