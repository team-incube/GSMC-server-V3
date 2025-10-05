package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.CreateFileService
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3Service
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class CreateFileServiceImpl(
    private val s3Service: S3Service,
    private val fileExposedRepository: FileExposedRepository,
) : CreateFileService {

    @Transactional
    override fun execute(file: MultipartFile): File {
        validateFile(file)

        val originalName = file.originalFilename ?: throw IllegalArgumentException("파일명이 없습니다")
        val storedName = generateStoredFileName(originalName)

        val fileUri = s3Service.uploadFile(file)

        return fileExposedRepository.saveFile(
            originalName = originalName,
            storedName = storedName,
            uri = fileUri
        )
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("파일이 비어있습니다")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다")
        }

        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("파일명이 없습니다")
        val extension = originalFilename.substringAfterLast(".", "").lowercase()

        if (extension !in ALLOWED_EXTENSIONS) {
            throw IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용되는 형식: ${ALLOWED_EXTENSIONS.joinToString(", ")}")
        }
    }

    private fun generateStoredFileName(originalFilename: String): String {
        val extension = originalFilename.substringAfterLast(".", "")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "${timestamp}_${uuid}.${extension}"
    }

    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        private val ALLOWED_EXTENSIONS = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", // 이미지
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 문서
            "txt", "hwp" // 기타
        )
    }
}
