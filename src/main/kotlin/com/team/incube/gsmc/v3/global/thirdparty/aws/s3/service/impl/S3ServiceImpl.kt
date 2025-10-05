package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.impl

import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data.S3Environment
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3Service
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class S3ServiceImpl(
    private val s3Client: S3Client,
    private val s3Environment: S3Environment,
) : S3Service {

    override fun uploadFile(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("파일명이 없습니다")
        val storedFilename = generateStoredFilename(originalFilename)

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Environment.bucketName)
            .key(storedFilename)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(file.inputStream, file.size)
        )

        return "https://${s3Environment.bucketName}.s3.amazonaws.com/$storedFilename"
    }

    override fun deleteFile(fileUri: String) {
        val key = extractKeyFromUri(fileUri)

        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(s3Environment.bucketName)
            .key(key)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }

    private fun generateStoredFilename(originalFilename: String): String {
        val extension = originalFilename.substringAfterLast(".", "")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "evidences/${timestamp}_${uuid}.${extension}"
    }

    private fun extractKeyFromUri(fileUri: String): String {
        return URLDecoder.decode(fileUri.substringAfter(".com/"), StandardCharsets.UTF_8)
    }
}
