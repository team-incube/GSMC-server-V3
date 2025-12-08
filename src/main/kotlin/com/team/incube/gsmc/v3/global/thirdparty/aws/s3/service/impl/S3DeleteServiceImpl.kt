package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.impl

import com.team.incube.gsmc.v3.global.config.logger
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.handler.S3ExceptionHandler
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier

@Service
class S3DeleteServiceImpl(
    private val s3Client: S3Client,
    private val s3Environment: S3Environment,
) : S3DeleteService {
    override fun execute(fileUri: String) {
        val key = extractKeyFromUri(fileUri)
        S3ExceptionHandler.handleDeleteOperation {
            s3Client.deleteObject { req ->
                req
                    .bucket(s3Environment.bucketName)
                    .key(key)
            }
        }
    }

    override fun execute(fileUris: List<String>) {
        if (fileUris.isEmpty()) return
        fileUris.chunked(1000).forEach { chunk ->
            val objectIdentifiers =
                chunk.map { fileUri ->
                    ObjectIdentifier
                        .builder()
                        .key(extractKeyFromUri(fileUri))
                        .build()
                }
            S3ExceptionHandler.handleDeleteOperation {
                val response =
                    s3Client.deleteObjects(
                        DeleteObjectsRequest
                            .builder()
                            .bucket(s3Environment.bucketName)
                            .delete(
                                Delete
                                    .builder()
                                    .objects(objectIdentifiers)
                                    .build(),
                            ).build(),
                    )
                if (response.hasErrors()) {
                    response.errors().forEach { error ->
                        logger().error(
                            "S3 object deletion failed - Key: ${error.key()}, Code: ${error.code()}, Message: ${error.message()}",
                        )
                    }
                }
            }
        }
    }

    private fun extractKeyFromUri(fileUri: String): String = fileUri.substringAfter(".com/")
}
