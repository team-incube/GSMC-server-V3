package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.request.CreatePresignedUploadUrlRequest
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreatePresignedUploadUrlResponse
import com.team.incube.gsmc.v3.domain.file.service.CreatePresignedUploadUrlService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class CreatePresignedUploadUrlServiceImpl(
    private val s3Presigner: S3Presigner,
    private val s3Environment: S3Environment,
) : CreatePresignedUploadUrlService {
    override fun execute(request: CreatePresignedUploadUrlRequest): CreatePresignedUploadUrlResponse {
        validateRequest(request)

        val fileExtension =
            StringUtils
                .getFilenameExtension(request.fileName)
                ?.lowercase()
                ?: throw GsmcException(ErrorCode.FILE_EXTENSION_NOT_FOUND)

        validateFileExtension(fileExtension)
        val fileKey = generateFileKey(fileExtension)

        val putObjectRequest =
            PutObjectRequest
                .builder()
                .bucket(s3Environment.bucketName)
                .key(fileKey)
                .contentType(request.contentType)
                .build()

        val presignRequest =
            PutObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES))
                .putObjectRequest(putObjectRequest)
                .build()

        val presignedRequest: PresignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest)
        val expiresAt = Instant.now().plus(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES))

        return CreatePresignedUploadUrlResponse(
            presignedUrl = presignedRequest.url().toString(),
            fileKey = fileKey,
            expiresAt = expiresAt,
        )
    }

    private fun validateRequest(request: CreatePresignedUploadUrlRequest) {
        if (request.fileSize > MAX_FILE_SIZE_BYTES) {
            throw GsmcException(ErrorCode.FILE_SIZE_EXCEEDED)
        }

        if (request.fileSize <= 0) {
            throw GsmcException(ErrorCode.FILE_EMPTY)
        }
    }

    private fun validateFileExtension(extension: String) {
        if (extension !in ALLOWED_EXTENSIONS) {
            throw GsmcException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED)
        }
    }

    private fun generateFileKey(fileExtension: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "file/${timestamp}_$uuid.$fileExtension"
    }

    companion object {
        private const val MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024 // 100MB
        private const val PRESIGNED_URL_EXPIRATION_MINUTES = 15L

        private val ALLOWED_EXTENSIONS =
            setOf(
                "jpg",
                "jpeg",
                "png",
                "gif",
                "bmp",
                "webp",
                "pdf",
                "doc",
                "docx",
                "csv",
                "xls",
                "xlsx",
                "ppt",
                "pptx",
                "txt",
                "hwp",
                "hwpx",
            )
    }
}
