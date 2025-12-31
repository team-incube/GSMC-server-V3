package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.request.ConfirmFileUploadRequest
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.ConfirmFileUploadService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException

@Service
class ConfirmFileUploadServiceImpl(
    private val s3Client: S3Client,
    private val s3Environment: S3Environment,
    private val currentMemberProvider: CurrentMemberProvider,
    private val fileExposedRepository: FileExposedRepository,
) : ConfirmFileUploadService {
    override fun execute(request: ConfirmFileUploadRequest): CreateFileResponse {
        verifyFileExistsInS3(request.fileKey)

        val fileUri = buildFileUri(request.fileKey)
        val storedName = extractStoredNameFromKey(request.fileKey)

        return transaction {
            val savedFile =
                fileExposedRepository.save(
                    userId = currentMemberProvider.getCurrentMember().id,
                    originalName = request.originalFileName,
                    storedName = storedName,
                    uri = fileUri,
                )
            CreateFileResponse(
                id = savedFile.id,
                originalName = savedFile.originalName,
                storeName = savedFile.storeName,
                uri = savedFile.uri,
            )
        }
    }

    private fun verifyFileExistsInS3(fileKey: String) {
        try {
            val headObjectRequest =
                HeadObjectRequest
                    .builder()
                    .bucket(s3Environment.bucketName)
                    .key(fileKey)
                    .build()

            s3Client.headObject(headObjectRequest)
        } catch (e: NoSuchKeyException) {
            throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        }
    }

    private fun buildFileUri(fileKey: String): String =
        s3Client
            .utilities()
            .getUrl { builder ->
                builder.bucket(s3Environment.bucketName).key(fileKey)
            }.toString()

    private fun extractStoredNameFromKey(fileKey: String): String = fileKey.substringAfterLast("/")
}
