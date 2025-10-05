package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.impl

import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3UploadService
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.util.S3ExceptionHandler
import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Template
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class S3UploadServiceImpl(
    private val s3Template: S3Template,
    private val s3Environment: S3Environment,
) : S3UploadService {

    override fun execute(file: MultipartFile): String {
        validateFile(file)

        val originalFilename = file.originalFilename
            ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        val fileExtension = StringUtils.getFilenameExtension(originalFilename)
            ?: throw GsmcException(ErrorCode.FILE_EXTENSION_NOT_FOUND)

        val storedFilename = generateStoredFilename(fileExtension)

        return S3ExceptionHandler.handleUploadOperation {
            val s3Resource = s3Template.upload(
                s3Environment.bucketName,
                storedFilename,
                file.inputStream,
                ObjectMetadata.builder()
                    .contentType(file.contentType)
                    .build()
            )
            s3Resource.url.toString()
        }
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw GsmcException(ErrorCode.FILE_EMPTY)
        }
    }

    private fun generateStoredFilename(fileExtension: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "evidences/${timestamp}_${uuid}.${fileExtension}"
    }
}
