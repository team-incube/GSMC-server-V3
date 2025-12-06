package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.DeleteFileService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.s3.S3FileDeletionEvent
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DeleteFileServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val eventPublisher: ApplicationEventPublisher,
) : DeleteFileService {
    override fun execute(fileId: Long) =
        transaction {
            val file =
                fileExposedRepository.findById(fileId)
                    ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            val currentMember = currentMemberProvider.getCurrentMember()
            if (currentMember.role == MemberRole.STUDENT && file.member != currentMember.id) {
                throw GsmcException(ErrorCode.FILE_UNAUTHORIZED_ACCESS)
            }
            fileExposedRepository.deleteById(fileId)
            eventPublisher.publishEvent(S3FileDeletionEvent(file.uri))
        }
}
