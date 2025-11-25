package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.PatchEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.UpdateEvidenceService
import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UpdateEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
) : UpdateEvidenceService {
    override fun execute(
        evidenceId: Long,
        participantId: Long?,
        title: String?,
        content: String?,
        fileIds: List<Long>?,
    ): PatchEvidenceResponse =
        transaction {
            val evidence =
                evidenceExposedRepository.findById(evidenceId)
                    ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)

            if (!fileIds.isNullOrEmpty() && !fileExposedRepository.existsByIdIn(fileIds)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            if (participantId != null) {
                if (!scoreExposedRepository.existsById(participantId)) {
                    throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
                }
                scoreExposedRepository.updateSourceIdToNull(evidenceId)
                scoreExposedRepository.updateSourceId(participantId, evidenceId)
            }

            val updatedEvidence =
                evidenceExposedRepository.update(
                    id = evidenceId,
                    title = title ?: evidence.title,
                    content = content ?: evidence.content,
                    fileIds = fileIds ?: evidence.files.map { it.fileId },
                )

            PatchEvidenceResponse(
                id = updatedEvidence.id,
                title = updatedEvidence.title,
                content = updatedEvidence.content,
                createAt = updatedEvidence.createdAt,
                updateAt = updatedEvidence.updatedAt,
                files =
                    updatedEvidence.files.map {
                        FileItem(
                            fileId = it.fileId,
                            fileOriginalName = it.fileOriginalName,
                            fileStoreName = it.fileStoreName,
                            fileUri = it.fileUri,
                            memberId = it.memberId,
                        )
                    },
            )
        }
}
