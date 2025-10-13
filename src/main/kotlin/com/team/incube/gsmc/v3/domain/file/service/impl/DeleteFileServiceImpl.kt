package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.DeleteFileService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteFileServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val s3DeleteService: S3DeleteService,
) : DeleteFileService {
    override fun execute(fileId: Long) =
        transaction {
            val file =
                fileExposedRepository.findById(fileId)
                    ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)

            s3DeleteService.execute(file.fileUri)

            fileExposedRepository.deleteById(fileId)
        }
}
