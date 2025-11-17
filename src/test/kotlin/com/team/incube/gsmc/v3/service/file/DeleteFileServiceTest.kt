package com.team.incube.gsmc.v3.service.file

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.impl.DeleteFileServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class DeleteFileServiceTest :
    BehaviorSpec({

        data class TestData(
            val mockFileRepository: FileExposedRepository,
            val mockCurrentMemberProvider: CurrentMemberProvider,
            val mockS3DeleteService: S3DeleteService,
            val deleteFileService: DeleteFileServiceImpl,
        )

        fun createTestContext(): TestData {
            val mockFileRepository = mockk<FileExposedRepository>()
            val mockCurrentMemberProvider = mockk<CurrentMemberProvider>()
            val mockS3DeleteService = mockk<S3DeleteService>()

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

            val deleteFileService = DeleteFileServiceImpl(mockFileRepository, mockCurrentMemberProvider, mockS3DeleteService)

            return TestData(
                mockFileRepository = mockFileRepository,
                mockCurrentMemberProvider = mockCurrentMemberProvider,
                mockS3DeleteService = mockS3DeleteService,
                deleteFileService = deleteFileService,
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

        Given("존재하는 파일 ID가 주어졌을 때") {
            val context = createTestContext()
            val fileId = 1L
            val testFileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/test-file.pdf"
            val existingFile =
                File(
                    fileId = fileId,
                    memberId = 0L,
                    fileOriginalName = "test-document.pdf",
                    fileStoredName = "20251015120000_abc123def456.pdf",
                    fileUri = testFileUri,
                )

            every { context.mockFileRepository.findById(fileId) } returns existingFile
            justRun { context.mockS3DeleteService.execute(testFileUri) }
            justRun { context.mockFileRepository.deleteById(fileId) }

            When("파일 삭제를 실행하면") {
                context.deleteFileService.execute(fileId)

                Then("파일 저장소에서 파일을 조회해야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findById(fileId) }
                }

                Then("S3에서 파일을 삭제해야 한다") {
                    verify(exactly = 1) { context.mockS3DeleteService.execute(testFileUri) }
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
                    verify(exactly = 0) { context.mockS3DeleteService.execute(any()) }
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
                        fileId = fileId,
                        memberId = 0L,
                        fileOriginalName = "file$fileId.pdf",
                        fileStoredName = "stored-file$fileId.pdf",
                        fileUri = fileUris[index],
                    )
                every { context.mockFileRepository.findById(fileId) } returns file
                justRun { context.mockS3DeleteService.execute(fileUris[index]) }
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
                    fileUris.forEach { fileUri ->
                        verify(exactly = 1) { context.mockS3DeleteService.execute(fileUri) }
                    }
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
                    fileId = fileId,
                    memberId = 0L,
                    fileOriginalName = "test-document.pdf",
                    fileStoredName = "20251015120000_abc123def456.pdf",
                    fileUri = testFileUri,
                )

            // Interaction - S3 삭제 시 예외 발생
            every { context.mockFileRepository.findById(fileId) } returns existingFile
            every { context.mockS3DeleteService.execute(testFileUri) } throws RuntimeException("S3 삭제 실패")

            When("파일 삭제를 실행하면") {
                Then("예외가 전파되어야 한다") {
                    shouldThrow<RuntimeException> {
                        context.deleteFileService.execute(fileId)
                    }
                }

                Then("파일 저장소 삭제는 호출되지 않아야 한다") {
                    verify(exactly = 0) { context.mockFileRepository.deleteById(any()) }
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
                            fileId = fileId,
                            memberId = 0L,
                            fileOriginalName = fileName,
                            fileStoredName = "stored-$fileName",
                            fileUri = fileUri,
                        )

                    every { context.mockFileRepository.findById(fileId) } returns file
                    justRun { context.mockS3DeleteService.execute(fileUri) }
                    justRun { context.mockFileRepository.deleteById(fileId) }

                    context.deleteFileService.execute(fileId)

                    Then("해당 파일이 정상적으로 삭제되어야 한다") {
                        verify(exactly = 1) { context.mockFileRepository.findById(fileId) }
                        verify(exactly = 1) { context.mockS3DeleteService.execute(fileUri) }
                        verify(exactly = 1) { context.mockFileRepository.deleteById(fileId) }
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
                    fileId = fileId,
                    memberId = 0L,
                    fileOriginalName = "test-document.pdf",
                    fileStoredName = "20251015120000_abc123def456.pdf",
                    fileUri = testFileUri,
                )

            every { context.mockFileRepository.findById(fileId) } returns existingFile
            justRun { context.mockS3DeleteService.execute(testFileUri) }
            justRun { context.mockFileRepository.deleteById(fileId) }

            When("파일 삭제를 실행하면") {
                context.deleteFileService.execute(fileId)

                Then("조회 → S3 삭제 → DB 삭제 순서로 실행되어야 한다") {
                    verify(exactly = 1) { context.mockFileRepository.findById(fileId) }
                    verify(exactly = 1) { context.mockS3DeleteService.execute(testFileUri) }
                    verify(exactly = 1) { context.mockFileRepository.deleteById(fileId) }
                }
            }
        }
    })
