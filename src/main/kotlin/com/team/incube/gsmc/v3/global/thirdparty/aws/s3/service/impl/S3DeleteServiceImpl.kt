package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.impl

import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.util.S3ExceptionHandler
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

    private fun extractKeyFromUri(fileUri: String): String {
        return fileUri.substringAfter(".com/")
    }
}
