package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceDraftResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceDraftRedisRepository
import com.team.incube.gsmc.v3.domain.evidence.service.FindMyEvidenceDraftService
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val evidenceDraftRedisRepository: EvidenceDraftRedisRepository,
    private val fileExposedRepository: FileExposedRepository,
) : FindMyEvidenceDraftService {
    override fun execute(): GetEvidenceDraftResponse? {
        val memberId = currentMemberProvider.getCurrentMemberId()
        val draftEntity = evidenceDraftRedisRepository.findById(memberId).orElse(null) ?: return null

        return transaction {
            val files =
                if (draftEntity.fileIds.isNotEmpty()) {
                    val foundFiles = fileExposedRepository.findAllByIdIn(draftEntity.fileIds)
                    foundFiles.map { file ->
                        GetFileResponse(
                            id = file.id,
                            memberId = file.member,
                            originalName = file.originalName,
                            storeName = file.storeName,
                            uri = file.uri,
                        )
                    }
                } else {
                    emptyList()
                }

            GetEvidenceDraftResponse(
                title = draftEntity.title,
                content = draftEntity.content,
                files = files,
            )
        }
    }
}
