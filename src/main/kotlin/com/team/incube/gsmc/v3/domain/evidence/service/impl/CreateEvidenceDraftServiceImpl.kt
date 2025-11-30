package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.entity.redis.EvidenceDraftRedisEntity
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.CreateEvidenceDraftRequest
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceDraftResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceDraftRedisRepository
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceDraftService
import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val fileExposedRepository: FileExposedRepository,
    private val evidenceDraftRedisRepository: EvidenceDraftRedisRepository,
) : CreateEvidenceDraftService {
    override fun execute(request: CreateEvidenceDraftRequest): GetEvidenceDraftResponse =
        transaction {
            val memberId = currentMemberProvider.getCurrentMemberId()

            val files =
                if (request.fileIds.isNotEmpty()) {
                    val foundFiles = fileExposedRepository.findAllByIdIn(request.fileIds)
                    if (foundFiles.size != request.fileIds.toSet().size) {
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

            val draftEntity =
                EvidenceDraftRedisEntity(
                    memberId = memberId,
                    title = request.title,
                    content = request.content,
                    fileIds = files.map { it.id },
                )
            evidenceDraftRedisRepository.save(draftEntity)

            GetEvidenceDraftResponse(
                title = request.title,
                content = request.content,
                files = files,
            )
        }
}
