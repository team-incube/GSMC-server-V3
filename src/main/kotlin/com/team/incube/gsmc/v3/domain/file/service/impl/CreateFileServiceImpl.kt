package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.CreateFileService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3UploadService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class CreateFileServiceImpl(
    private val s3UploadService: S3UploadService,
    private val currentMemberProvider: CurrentMemberProvider,
    private val fileExposedRepository: FileExposedRepository,
) : CreateFileService {
    override fun execute(file: MultipartFile): CreateFileResponse {
        validateFile(file)
        val originalName = file.originalFilename!!
        val storedName = generateStoredFileName(originalName)
        val fileUri = s3UploadService.execute(file)

        return transaction {
            val savedFile =
                fileExposedRepository.saveFile(
                    userId = currentMemberProvider.getCurrentMember().id,
                    originalName = originalName,
                    storedName = storedName,
                    uri = fileUri,
                )
            CreateFileResponse(
                id = savedFile.id,
                fileOriginalName = savedFile.originalName,
                fileStoreName = savedFile.storeName,
                fileUri = savedFile.uri,
            )
        }
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw GsmcException(ErrorCode.FILE_EMPTY)
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
