package com.team.incube.gsmc.v3.service.file

import com.team.incube.gsmc.v3.domain.file.presentation.data.request.CreatePresignedUploadUrlRequest
import com.team.incube.gsmc.v3.domain.file.service.impl.CreatePresignedUploadUrlServiceImpl
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI

class CreatePresignedUploadUrlServiceTest :
    BehaviorSpec({

        data class TestData(
            val mockS3Presigner: S3Presigner,
            val s3Environment: S3Environment,
            val createPresignedUploadUrlService: CreatePresignedUploadUrlServiceImpl,
        )

        fun createTestContext(): TestData {
            val mockS3Presigner = mockk<S3Presigner>()
            val s3Environment = S3Environment(bucketName = "test-bucket")

            val createPresignedUploadUrlService = CreatePresignedUploadUrlServiceImpl(mockS3Presigner, s3Environment)

            return TestData(
                mockS3Presigner = mockS3Presigner,
                s3Environment = s3Environment,
                createPresignedUploadUrlService = createPresignedUploadUrlService,
            )
        }

        Given("유효한 파일 정보가 주어졌을 때") {
            val context = createTestContext()
            val request =
                CreatePresignedUploadUrlRequest(
                    fileName = "test-document.pdf",
                    fileSize = 1024 * 1024, // 1MB
                    contentType = "application/pdf",
                )

            val mockPresignedRequest =
                mockk<PresignedPutObjectRequest> {
                    every { url() } returns URI.create("https://test-bucket.s3.amazonaws.com/file/20250101120000_abc123.pdf?signature=xyz").toURL()
                }

            every { context.mockS3Presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns mockPresignedRequest

            When("Pre-signed URL 생성을 실행하면") {
                val result = context.createPresignedUploadUrlService.execute(request)

                Then("Pre-signed URL이 정상적으로 생성되어야 한다") {
                    result shouldNotBe null
                    result.presignedUrl shouldNotBe null
                    result.fileKey shouldNotBe null
                    result.expiresAt shouldNotBe null
                }

                Then("파일 키는 'file/' 접두사를 포함해야 한다") {
                    result.fileKey shouldContain "file/"
                }

                Then("파일 키는 .pdf 확장자를 포함해야 한다") {
                    result.fileKey shouldContain ".pdf"
                }

                Then("S3 Presigner가 호출되어야 한다") {
                    verify(exactly = 1) { context.mockS3Presigner.presignPutObject(any<PutObjectPresignRequest>()) }
                }
            }
        }

        Given("허용된 다양한 확장자의 파일들이 주어졌을 때") {
            val allowedExtensions = listOf("jpg", "png", "pdf", "docx", "xlsx", "pptx", "hwp")

            allowedExtensions.forEach { extension ->
                When("$extension 파일 정보로 Pre-signed URL을 생성하면") {
                    val context = createTestContext()
                    val request =
                        CreatePresignedUploadUrlRequest(
                            fileName = "test-file.$extension",
                            fileSize = 1024 * 1024,
                            contentType = "application/$extension",
                        )

                    val mockPresignedRequest =
                        mockk<PresignedPutObjectRequest> {
                            every { url() } returns URI.create("https://test-bucket.s3.amazonaws.com/file/test.$extension?signature=xyz").toURL()
                        }

                    every { context.mockS3Presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns mockPresignedRequest

                    val result = context.createPresignedUploadUrlService.execute(request)

                    Then("Pre-signed URL이 정상적으로 생성되어야 한다") {
                        result shouldNotBe null
                        result.fileKey shouldContain ".$extension"
                    }
                }
            }
        }

        Given("파일 크기가 100MB를 초과하는 파일이 주어졌을 때") {
            val context = createTestContext()
            val request =
                CreatePresignedUploadUrlRequest(
                    fileName = "large-file.pdf",
                    fileSize = 101L * 1024 * 1024, // 101MB
                    contentType = "application/pdf",
                )

            When("Pre-signed URL 생성을 실행하면") {
                Then("FILE_SIZE_EXCEEDED 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.createPresignedUploadUrlService.execute(request)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_SIZE_EXCEEDED
                }
            }
        }

        Given("파일 크기가 0 이하인 파일이 주어졌을 때") {
            val context = createTestContext()
            val request =
                CreatePresignedUploadUrlRequest(
                    fileName = "empty-file.pdf",
                    fileSize = 0,
                    contentType = "application/pdf",
                )

            When("Pre-signed URL 생성을 실행하면") {
                Then("FILE_EMPTY 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.createPresignedUploadUrlService.execute(request)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_EMPTY
                }
            }
        }

        Given("허용되지 않은 확장자의 파일이 주어졌을 때") {
            val notAllowedExtensions = listOf("exe", "bat", "sh", "js", "php", "asp")

            notAllowedExtensions.forEach { extension ->
                When("$extension 파일 정보로 Pre-signed URL을 생성하면") {
                    val context = createTestContext()
                    val request =
                        CreatePresignedUploadUrlRequest(
                            fileName = "malicious-file.$extension",
                            fileSize = 1024,
                            contentType = "application/$extension",
                        )

                    Then("FILE_EXTENSION_NOT_ALLOWED 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<GsmcException> {
                                context.createPresignedUploadUrlService.execute(request)
                            }
                        exception.errorCode shouldBe ErrorCode.FILE_EXTENSION_NOT_ALLOWED
                    }
                }
            }
        }

        Given("확장자가 없는 파일이 주어졌을 때") {
            val context = createTestContext()
            val request =
                CreatePresignedUploadUrlRequest(
                    fileName = "file-without-extension",
                    fileSize = 1024,
                    contentType = "application/octet-stream",
                )

            When("Pre-signed URL 생성을 실행하면") {
                Then("FILE_EXTENSION_NOT_FOUND 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.createPresignedUploadUrlService.execute(request)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_EXTENSION_NOT_FOUND
                }
            }
        }

        Given("대문자 확장자를 가진 파일이 주어졌을 때") {
            val context = createTestContext()
            val request =
                CreatePresignedUploadUrlRequest(
                    fileName = "TEST-FILE.PDF",
                    fileSize = 1024 * 1024,
                    contentType = "application/pdf",
                )

            val mockPresignedRequest =
                mockk<PresignedPutObjectRequest> {
                    every { url() } returns URI.create("https://test-bucket.s3.amazonaws.com/file/test.pdf?signature=xyz").toURL()
                }

            every { context.mockS3Presigner.presignPutObject(any<PutObjectPresignRequest>()) } returns mockPresignedRequest

            When("Pre-signed URL 생성을 실행하면") {
                val result = context.createPresignedUploadUrlService.execute(request)

                Then("확장자가 소문자로 정규화되어야 한다") {
                    result shouldNotBe null
                    result.fileKey shouldContain ".pdf"
                }
            }
        }
    })
