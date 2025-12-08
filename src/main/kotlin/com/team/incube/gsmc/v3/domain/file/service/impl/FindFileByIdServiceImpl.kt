package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.FindFileByIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindFileByIdServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindFileByIdService {
    override fun execute(fileId: Long): GetFileResponse =
        transaction {
            val file =
                fileExposedRepository.findById(fileId)
                    ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)

            val currentMember = currentMemberProvider.getCurrentMember()

            if (file.member != currentMember.id) {
                throw GsmcException(ErrorCode.FILE_UNAUTHORIZED_ACCESS)
            }
            GetFileResponse(
                id = file.id,
                memberId = file.member,
                originalName = file.originalName,
                storeName = file.storeName,
                uri = file.uri,
            )
        }
}
