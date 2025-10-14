package com.team.incube.gsmc.v3.service.file

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.impl.CreateFileServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3UploadService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.multipart.MultipartFile

class CreateFileServiceTest :
    BehaviorSpec({

        // Data - 테스트에 필요한 데이터 정의
        data class TestData(
            val validFile: MultipartFile,
            val emptyFile: MultipartFile,
            val fileWithoutExtension: MultipartFile,
            val invalidExtensionFile: MultipartFile,
            val mockS3UploadService: S3UploadService,
            val mockFileRepository: FileExposedRepository,
            val createFileService: CreateFileServiceImpl,
        )

        // Context - 테스트 컨텍스트 초기화
        fun createTestContext(): TestData {
            val mockS3UploadService = mockk<S3UploadService>()
            val mockFileRepository = mockk<FileExposedRepository>()
            val createFileService = CreateFileServiceImpl(mockS3UploadService, mockFileRepository)

            val validFile =
                mockk<MultipartFile> {
                    every { isEmpty } returns false
                    every { originalFilename } returns "test-document.pdf"
                }

            val emptyFile =
                mockk<MultipartFile> {
                    every { isEmpty } returns true
                    every { originalFilename } returns "empty-file.pdf"
                }

            val fileWithoutExtension =
                mockk<MultipartFile> {
                    every { isEmpty } returns false
                    every { originalFilename } returns null
                }

            val invalidExtensionFile =
                mockk<MultipartFile> {
                    every { isEmpty } returns false
                    every { originalFilename } returns "malware.exe"
                }

            return TestData(
                validFile = validFile,
                emptyFile = emptyFile,
                fileWithoutExtension = fileWithoutExtension,
                invalidExtensionFile = invalidExtensionFile,
                mockS3UploadService = mockS3UploadService,
                mockFileRepository = mockFileRepository,
                createFileService = createFileService,
            )
        }

        beforeSpec {
            // transaction 블록을 mock하여 실제 트랜잭션 없이 코드 실행
            mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
            every {
                transaction(db = any(), statement = any<Transaction.() -> Any>())
            } answers {
                secondArg<Transaction.() -> Any>().invoke(mockk(relaxed = true))
            }
        }

        Given("유효한 파일이 주어졌을 때") {
            val context = createTestContext()
            val testFileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/test-file.pdf"

            // Interaction - Mock 객체 동작 정의
            every { context.mockS3UploadService.execute(context.validFile) } returns testFileUri
            every {
                context.mockFileRepository.saveFile(
                    originalName = "test-document.pdf",
                    storedName = any(),
                    uri = testFileUri,
                )
            } returns
                File(
                    fileId = 1L,
                    fileOriginalName = "test-document.pdf",
                    fileStoredName = "20251015120000_abc123def456.pdf",
                    fileUri = testFileUri,
                )

            When("파일 생성을 실행하면") {
                val result = context.createFileService.execute(context.validFile)

                Then("파일이 정상적으로 생성되어야 한다") {
                    result shouldNotBe null
                    result.id shouldBe 1L
                    result.fileOriginalName shouldBe "test-document.pdf"
                    result.fileStoredName shouldContain "pdf"
                    result.fileUri shouldBe testFileUri
                }

                Then("S3에 파일이 업로드되어야 한다") {
                    verify(exactly = 1) { context.mockS3UploadService.execute(context.validFile) }
                }

                Then("저장된 파일명은 타임스탬프와 UUID를 포함해야 한다") {
                    result.fileStoredName shouldContain "_"
                    result.fileStoredName shouldEndWith ".pdf"
                }

                Then("파일 저장소에 저장되어야 한다") {
                    verify(exactly = 1) {
                        context.mockFileRepository.saveFile(
                            originalName = "test-document.pdf",
                            storedName = any(),
                            uri = testFileUri,
                        )
                    }
                }
            }
        }

        Given("허용된 다양한 확장자의 파일들이 주어졌을 때") {
            val allowedExtensions = listOf("jpg", "png", "pdf", "docx", "xlsx", "pptx", "hwp")

            allowedExtensions.forEach { extension ->
                When("$extension 파일을 업로드하면") {
                    val context = createTestContext()
                    val testFile =
                        mockk<MultipartFile> {
                            every { isEmpty } returns false
                            every { originalFilename } returns "test-file.$extension"
                        }
                    val testFileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/test-file.$extension"

                    every { context.mockS3UploadService.execute(testFile) } returns testFileUri
                    every {
                        context.mockFileRepository.saveFile(
                            originalName = "test-file.$extension",
                            storedName = any(),
                            uri = testFileUri,
                        )
                    } returns
                        File(
                            fileId = 1L,
                            fileOriginalName = "test-file.$extension",
                            fileStoredName = "20251015120000_test.$extension",
                            fileUri = testFileUri,
                        )

                    val result = context.createFileService.execute(testFile)

                    Then("파일이 정상적으로 업로드되어야 한다") {
                        result shouldNotBe null
                        result.fileOriginalName shouldBe "test-file.$extension"
                    }
                }
            }
        }

        Given("빈 파일이 주어졌을 때") {
            val context = createTestContext()

            When("파일 생성을 실행하면") {
                Then("FILE_EMPTY 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.createFileService.execute(context.emptyFile)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_EMPTY
                }
            }
        }

        Given("파일명이 없는 파일이 주어졌을 때") {
            val context = createTestContext()

            When("파일 생성을 실행하면") {
                Then("FILE_NOT_FOUND 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.createFileService.execute(context.fileWithoutExtension)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }
        }

        Given("허용되지 않은 확장자의 파일이 주어졌을 때") {
            val context = createTestContext()

            When("파일 생성을 실행하면") {
                Then("FILE_EXTENSION_NOT_ALLOWED 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.createFileService.execute(context.invalidExtensionFile)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_EXTENSION_NOT_ALLOWED
                }
            }
        }

        Given("허용되지 않은 다양한 확장자의 파일들이 주어졌을 때") {
            val notAllowedExtensions = listOf("exe", "bat", "sh", "js", "php", "asp")

            notAllowedExtensions.forEach { extension ->
                When("$extension 파일을 업로드하면") {
                    val context = createTestContext()
                    val testFile =
                        mockk<MultipartFile> {
                            every { isEmpty } returns false
                            every { originalFilename } returns "malicious-file.$extension"
                        }

                    Then("FILE_EXTENSION_NOT_ALLOWED 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<GsmcException> {
                                context.createFileService.execute(testFile)
                            }
                        exception.errorCode shouldBe ErrorCode.FILE_EXTENSION_NOT_ALLOWED
                    }
                }
            }
        }

        Given("대문자 확장자를 가진 파일이 주어졌을 때") {
            val context = createTestContext()
            val upperCaseFile =
                mockk<MultipartFile> {
                    every { isEmpty } returns false
                    every { originalFilename } returns "TEST-FILE.PDF"
                }
            val testFileUri = "https://gsmc-bucket.s3.amazonaws.com/evidences/test-file.PDF"

            every { context.mockS3UploadService.execute(upperCaseFile) } returns testFileUri
            every {
                context.mockFileRepository.saveFile(
                    originalName = "TEST-FILE.PDF",
                    storedName = any(),
                    uri = testFileUri,
                )
            } returns
                File(
                    fileId = 1L,
                    fileOriginalName = "TEST-FILE.PDF",
                    fileStoredName = "20251015120000_test.PDF",
                    fileUri = testFileUri,
                )

            When("파일 생성을 실행하면") {
                val result = context.createFileService.execute(upperCaseFile)

                Then("확장자 대소문자 구분 없이 정상 처리되어야 한다") {
                    result shouldNotBe null
                    result.fileOriginalName shouldBe "TEST-FILE.PDF"
                }
            }
        }
    })
