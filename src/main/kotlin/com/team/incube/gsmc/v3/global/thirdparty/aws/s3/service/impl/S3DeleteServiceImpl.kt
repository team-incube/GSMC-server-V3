package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.impl

import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.handler.S3ExceptionHandler
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import io.awspring.cloud.s3.S3Template
import org.springframework.stereotype.Service

@Service
class S3DeleteServiceImpl(
    private val s3Template: S3Template,
    private val s3Environment: S3Environment,
) : S3DeleteService {
    override fun execute(fileUri: String) {
        val key = extractKeyFromUri(fileUri)

        S3ExceptionHandler.handleDeleteOperation {
            s3Template.deleteObject(s3Environment.bucketName, key)
        }
    }

    override fun execute(fileUris: List<String>) {
        if (fileUris.isEmpty()) return

        val keys = fileUris.map { extractKeyFromUri(it) }

        S3ExceptionHandler.handleDeleteOperation {
            s3Template.deleteObjects(s3Environment.bucketName, keys)
        }
    }

    private fun extractKeyFromUri(fileUri: String): String = fileUri.substringAfter(".com/")
}
