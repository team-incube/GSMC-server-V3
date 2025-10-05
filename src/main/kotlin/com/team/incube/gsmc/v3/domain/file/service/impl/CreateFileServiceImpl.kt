package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.CreateFileService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3UploadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class CreateFileServiceImpl(
    private val s3UploadService: S3UploadService,
    private val fileExposedRepository: FileExposedRepository,
) : CreateFileService {
    @Transactional
    override fun execute(file: MultipartFile): CreateFileResponse {
        validateFile(file)

        val originalName = file.originalFilename ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        val storedName = generateStoredFileName(originalName)

        val fileUri = s3UploadService.execute(file)

        val savedFile =
            fileExposedRepository.saveFile(
                originalName = originalName,
                storedName = storedName,
                uri = fileUri,
            )

        return CreateFileResponse(
            id = savedFile.fileId,
            fileOriginalName = savedFile.fileOriginalName,
            fileStoredName = savedFile.fileStoredName,
            fileUri = savedFile.fileUri,
        )
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw GsmcException(ErrorCode.FILE_EMPTY)
        }

        if (file.size > MAX_FILE_SIZE) {
            throw GsmcException(ErrorCode.FILE_SIZE_EXCEEDED)
        }

        val originalFilename = file.originalFilename ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        val extension = originalFilename.substringAfterLast(".", "").lowercase()

        if (extension !in ALLOWED_EXTENSIONS) {
            throw GsmcException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED)
        }
    }

    private fun generateStoredFileName(originalFilename: String): String {
        val extension = originalFilename.substringAfterLast(".", "")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val uuid = UUID.randomUUID().toString().replace("-", "")
        return "${timestamp}_$uuid.$extension"
    }

    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        private val ALLOWED_EXTENSIONS =
            setOf(
                "jpg",
                "jpeg",
                "png",
                "gif",
                "bmp",
                "webp", // 이미지
                "pdf",
                "doc",
                "docx",
                "xls",
                "xlsx",
                "ppt",
                "pptx", // 문서
                "txt",
                "hwp", // 기타
            )
    }
}
