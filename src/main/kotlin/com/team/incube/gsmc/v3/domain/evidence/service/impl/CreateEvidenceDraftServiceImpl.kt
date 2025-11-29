package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.CreateEvidenceDraftRequest
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceDraftResponse
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceDraftService
import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service

@Service
class CreateEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val fileExposedRepository: FileExposedRepository,
) : CreateEvidenceDraftService {
    @CachePut(
        value = ["evidenceDraft"],
        key = "#root.target.getMemberId()",
    )
    override fun execute(request: CreateEvidenceDraftRequest): GetEvidenceDraftResponse =
        transaction {
            val files =
                if (request.fileIds.isNotEmpty()) {
                    val foundFiles = fileExposedRepository.findAllByIdIn(request.fileIds)
                    if (foundFiles.size != request.fileIds.size) {
                        throw GsmcException(ErrorCode.FILE_NOT_FOUND)
                    }
                    foundFiles.map { file ->
                        FileItem(
                            id = file.id,
                            member = file.member,
                            originalName = file.originalName,
                            storeName = file.storeName,
                            uri = file.uri,
                        )
                    }
                } else {
                    emptyList()
                }

            GetEvidenceDraftResponse(
                title = request.title,
                content = request.content,
                files = files,
            )
        }

    fun getMemberId(): Long = currentMemberProvider.getCurrentMemberId()
}
