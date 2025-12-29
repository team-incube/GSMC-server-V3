package com.team.incube.gsmc.v3.service.file

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.presentation.data.request.ConfirmFileUploadRequest
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.impl.ConfirmFileUploadServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.NoSuchKeyException

class ConfirmFileUploadServiceTest :
    BehaviorSpec({

        data class TestData(
            val mockS3Client: S3Client,
            val s3Environment: S3Environment,
            val mockCurrentMemberProvider: CurrentMemberProvider,
            val mockFileRepository: FileExposedRepository,
            val confirmFileUploadService: ConfirmFileUploadServiceImpl,
        )

        fun createTestContext(): TestData {
            val mockS3Client = mockk<S3Client>()
            val s3Environment = S3Environment(bucketName = "test-bucket")
            val mockCurrentMemberProvider = mockk<CurrentMemberProvider>()
            val mockFileRepository = mockk<FileExposedRepository>()

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

            val confirmFileUploadService =
                ConfirmFileUploadServiceImpl(
                    mockS3Client,
                    s3Environment,
                    mockCurrentMemberProvider,
                    mockFileRepository,
                )

            return TestData(
                mockS3Client = mockS3Client,
                s3Environment = s3Environment,
                mockCurrentMemberProvider = mockCurrentMemberProvider,
                mockFileRepository = mockFileRepository,
                confirmFileUploadService = confirmFileUploadService,
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

        Given("S3에 업로드된 유효한 파일이 주어졌을 때") {
            val context = createTestContext()
            val request =
                ConfirmFileUploadRequest(
                    fileKey = "file/20250101120000_abc123.pdf",
                    originalFileName = "test-document.pdf",
                )

            val mockHeadObjectResponse = mockk<HeadObjectResponse>()

            every { context.mockS3Client.headObject(any<HeadObjectRequest>()) } returns mockHeadObjectResponse
            every {
                context.mockFileRepository.saveFile(
                    userId = 1L,
                    originalName = "test-document.pdf",
                    storedName = "20250101120000_abc123.pdf",
                    uri = "https://test-bucket.s3.amazonaws.com/file/20250101120000_abc123.pdf",
                )
            } returns
                File(
                    id = 1L,
                    member = 1L,
                    originalName = "test-document.pdf",
                    storeName = "20250101120000_abc123.pdf",
                    uri = "https://test-bucket.s3.amazonaws.com/file/20250101120000_abc123.pdf",
                )

            When("파일 업로드 확인을 실행하면") {
                val result = context.confirmFileUploadService.execute(request)

                Then("파일이 정상적으로 DB에 등록되어야 한다") {
                    result shouldNotBe null
                    result.id shouldBe 1L
                    result.originalName shouldBe "test-document.pdf"
                    result.storeName shouldBe "20250101120000_abc123.pdf"
                    result.uri shouldBe "https://test-bucket.s3.amazonaws.com/file/20250101120000_abc123.pdf"
                }

                Then("S3에서 파일 존재 여부를 확인해야 한다") {
                    verify(exactly = 1) { context.mockS3Client.headObject(any<HeadObjectRequest>()) }
                }

                Then("파일 저장소에 저장되어야 한다") {
                    verify(exactly = 1) {
                        context.mockFileRepository.saveFile(
                            userId = 1L,
                            originalName = "test-document.pdf",
                            storedName = "20250101120000_abc123.pdf",
                            uri = any(),
                        )
                    }
                }
            }
        }

        Given("S3에 존재하지 않는 파일이 주어졌을 때") {
            val context = createTestContext()
            val request =
                ConfirmFileUploadRequest(
                    fileKey = "file/nonexistent.pdf",
                    originalFileName = "test-document.pdf",
                )

            every { context.mockS3Client.headObject(any<HeadObjectRequest>()) } throws
                NoSuchKeyException
                    .builder()
                    .message("The specified key does not exist.")
                    .build()

            When("파일 업로드 확인을 실행하면") {
                Then("FILE_NOT_FOUND 예외가 발생해야 한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            context.confirmFileUploadService.execute(request)
                        }
                    exception.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }
        }

        Given("다양한 확장자의 파일들이 S3에 업로드되었을 때") {
            val fileExtensions = listOf("jpg", "png", "pdf", "docx", "xlsx", "hwp")

            fileExtensions.forEach { extension ->
                When("$extension 파일의 업로드를 확인하면") {
                    val context = createTestContext()
                    val fileKey = "file/20250101120000_test.$extension"
                    val request =
                        ConfirmFileUploadRequest(
                            fileKey = fileKey,
                            originalFileName = "test-file.$extension",
                        )

                    val mockHeadObjectResponse = mockk<HeadObjectResponse>()

                    every { context.mockS3Client.headObject(any<HeadObjectRequest>()) } returns mockHeadObjectResponse
                    every {
                        context.mockFileRepository.saveFile(
                            userId = 1L,
                            originalName = "test-file.$extension",
                            storedName = "20250101120000_test.$extension",
                            uri = any(),
                        )
                    } returns
                        File(
                            id = 1L,
                            member = 1L,
                            originalName = "test-file.$extension",
                            storeName = "20250101120000_test.$extension",
                            uri = "https://test-bucket.s3.amazonaws.com/$fileKey",
                        )

                    val result = context.confirmFileUploadService.execute(request)

                    Then("파일이 정상적으로 DB에 등록되어야 한다") {
                        result shouldNotBe null
                        result.originalName shouldBe "test-file.$extension"
                    }
                }
            }
        }
    })
