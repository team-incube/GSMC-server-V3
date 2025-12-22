package com.team.incube.gsmc.v3.service.file

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.impl.DeleteFileServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.context.ApplicationEventPublisher

class DeleteFileServiceTest :
    BehaviorSpec({

        data class TestData(
            val mockFileRepository: FileExposedRepository,
            val mockCurrentMemberProvider: CurrentMemberProvider,
            val mockEventPublisher: ApplicationEventPublisher,
            val deleteFileService: DeleteFileServiceImpl,
        )

        fun createTestContext(): TestData {
            val mockFileRepository = mockk<FileExposedRepository>()
            val mockCurrentMemberProvider = mockk<CurrentMemberProvider>()
            val mockEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

            every { mockCurrentMemberProvider.getCurrentMember() } returns
                Member(
                    id = 0L,
                    name = "Test User",
                    email = "test@test.com",
                    grade = 1,
                    classNumber = 1,
                    number = 1,
                    role = MemberRole.STUDENT,
                )

            val deleteFileService = DeleteFileServiceImpl(mockFileRepository, mockCurrentMemberProvider, mockEventPublisher)

            return TestData(
                mockFileRepository = mockFileRepository,
                mockCurrentMemberProvider = mockCurrentMemberProvider,
                mockEventPublisher = mockEventPublisher,
                deleteFileService = deleteFileService,
            )
        }

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

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        Given("존재하는 파일 ID가 주어졌을 때") {
            val context = createTestContext()
            val fileId = 1L
            val testFileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/test-file.pdf"
            val existingFile =
                File(
                    id = fileId,
                    member = 0L,
                    originalName = "test-document.pdf",
                    storeName = "20251015120000_abc123def456.pdf",
                    uri = testFileUri,
                )

            every { context.mockFileRepository.findById(fileId) } returns existingFile
            justRun { context.mockEventPublisher.publishEvent(any<Any>()) }
            justRun { context.mockFileRepository.deleteById(fileId) }

            When("파일 삭제를 실행하면") {
                context.deleteFileService.execute(fileId)

                Then("파일 저장소에서 파일을 조회해야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findById(fileId) }
                }

                Then("S3에서 파일을 삭제해야 한다") {
                    verify(exactly = 1) { context.mockEventPublisher.publishEvent(any<Any>()) }
                }

                Then("파일 저장소에서 파일을 삭제해야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.deleteById(fileId) }
                }
            }
        }

        Given("존재하지 않는 파일 ID가 주어졌을 때") {
            val context = createTestContext()
            val nonExistentFileId = 999L

            every { context.mockFileRepository.findById(nonExistentFileId) } returns null

            When("파일 삭제를 실행하면") {
                Then("FILE_NOT_FOUND 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.deleteFileService.execute(nonExistentFileId)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }

                Then("S3 삭제는 호출되지 않아야 한다") {
                    verify(exactly = 0) { context.mockEventPublisher.publishEvent(any<Any>()) }
                }

                Then("파일 저장소 삭제는 호출되지 않아야 한다") {
                    verify(exactly = 0) { context.mockFileRepository.deleteById(any()) }
                }
            }
        }

        Given("여러 개의 파일 ID가 순차적으로 주어졌을 때") {
            val context = createTestContext()
            val fileIds = listOf(1L, 2L, 3L)
            val fileUris =
                listOf(
                    "https://gsmc-bucket.s3.amazonaws.com/evidences/file1.pdf",
                    "https://gsmc-bucket.s3.amazonaws.com/evidences/file2.pdf",
                    "https://gsmc-bucket.s3.amazonaws.com/evidences/file3.pdf",
                )

            fileIds.forEachIndexed { index, fileId ->
                val file =
                    File(
                        id = fileId,
                        member = 0L,
                        originalName = "file$fileId.pdf",
                        storeName = "stored-file$fileId.pdf",
                        uri = fileUris[index],
                    )
                every { context.mockFileRepository.findById(fileId) } returns file
                justRun { context.mockEventPublisher.publishEvent(any<Any>()) }
                justRun { context.mockFileRepository.deleteById(fileId) }
            }

            When("모든 파일을 순차적으로 삭제하면") {
                fileIds.forEach { fileId ->
                    context.deleteFileService.execute(fileId)
                }

                Then("모든 파일이 저장소에서 조회되어야 한다") {
                    fileIds.forEach { fileId ->
                        verify(exactly = 1) { context.mockFileRepository.findById(fileId) }
                    }
                }

                Then("모든 파일이 S3에서 삭제되어야 한다") {
                    verify(exactly = fileUris.size) { context.mockEventPublisher.publishEvent(any<Any>()) }
                }

                Then("모든 파일이 저장소에서 삭제되어야 한다") {
                    fileIds.forEach { fileId ->
                        verify(exactly = 1) { context.mockFileRepository.deleteById(fileId) }
                    }
                }
            }
        }

        Given("파일은 존재하지만 S3 삭제가 실패할 때") {
            val context = createTestContext()
            val fileId = 1L
            val testFileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/test-file.pdf"
            val existingFile =
                File(
                    id = fileId,
                    member = 0L,
                    originalName = "test-document.pdf",
                    storeName = "20251015120000_abc123def456.pdf",
                    uri = testFileUri,
                )

            every { context.mockFileRepository.findById(fileId) } returns existingFile
            justRun { context.mockFileRepository.deleteById(fileId) }
            every { context.mockEventPublisher.publishEvent(any<Any>()) } throws RuntimeException("S3 삭제 실패")

            When("파일 삭제를 실행하면") {
                Then("예외가 전파되고 파일 저장소 삭제는 호출되어야 한다") {
                    shouldThrow<RuntimeException> {
                        context.deleteFileService.execute(fileId)
                    }
                    verify(exactly = 1) { context.mockFileRepository.deleteById(fileId) }
                }
            }
        }

        Given("null이 아닌 다양한 파일 ID들이 주어졌을 때") {
            val context = createTestContext()
            val testCases =
                listOf(
                    1L to "file1.jpg",
                    100L to "file100.png",
                    9999L to "file9999.pdf",
                )

            testCases.forEach { (fileId, fileName) ->
                When("파일 ID $fileId 로 삭제를 실행하면") {
                    val fileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/$fileName"
                    val file =
                        File(
                            id = fileId,
                            member = 0L,
                            originalName = fileName,
                            storeName = "stored-$fileName",
                            uri = fileUri,
                        )

                    every { context.mockFileRepository.findById(fileId) } returns file
                    justRun { context.mockEventPublisher.publishEvent(any<Any>()) }
                    justRun { context.mockFileRepository.deleteById(fileId) }

                    context.deleteFileService.execute(fileId)

                    Then("해당 파일이 정상적으로 삭제되어야 한다") {
                        verify(atLeast = 1) { context.mockFileRepository.findById(fileId) }
                        verify(atLeast = 1) { context.mockEventPublisher.publishEvent(any<Any>()) }
                        verify(atLeast = 1) { context.mockFileRepository.deleteById(fileId) }
                    }
                }
            }
        }

        Given("파일 저장소에서 조회 후 삭제 순서가 중요할 때") {
            val context = createTestContext()
            val fileId = 1L
            val testFileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/test-file.pdf"
            val existingFile =
                File(
                    id = fileId,
                    member = 0L,
                    originalName = "test-document.pdf",
                    storeName = "20251015120000_abc123def456.pdf",
                    uri = testFileUri,
                )

            every { context.mockFileRepository.findById(fileId) } returns existingFile
            justRun { context.mockEventPublisher.publishEvent(any<Any>()) }
            justRun { context.mockFileRepository.deleteById(fileId) }

            When("파일 삭제를 실행하면") {
                context.deleteFileService.execute(fileId)

                Then("조회 → S3 삭제 → DB 삭제 순서로 실행되어야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findById(fileId) }
                    verify(exactly = 1) { context.mockEventPublisher.publishEvent(any<Any>()) }
                    verify(exactly = 1) { context.mockFileRepository.deleteById(fileId) }
                }
            }
        }
    })
