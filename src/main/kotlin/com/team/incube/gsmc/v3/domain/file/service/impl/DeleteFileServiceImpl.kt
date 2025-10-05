package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.DeleteFileService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteFileServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val s3DeleteService: S3DeleteService,
) : DeleteFileService {
    @Transactional
    override fun execute(fileId: Long) {
        // 파일 존재 여부 확인
        val file =
            fileExposedRepository.findById(fileId)
                ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)

        // S3에서 파일 삭제
        file.fileUri?.let { uri ->
            s3DeleteService.execute(uri)
        }

        // 데이터베이스에서 파일 정보 삭제
        fileExposedRepository.deleteById(fileId)
    }
}
