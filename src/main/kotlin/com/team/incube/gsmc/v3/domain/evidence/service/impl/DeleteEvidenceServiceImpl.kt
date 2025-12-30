package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.DeleteEvidenceService
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.s3.S3BulkFileDeletionEvent
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DeleteEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : DeleteEvidenceService {
    override fun execute(evidenceId: Long) =
        transaction {
            val evidence =
                evidenceExposedRepository.findById(evidenceId)
                    ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)

            val (fileUris, fileIds) = evidence.files.map { it.uri to it.id }.unzip()
            if (fileIds.isNotEmpty()) {
                fileExposedRepository.deleteAllByIdIn(fileIds)
                eventPublisher.publishEvent(S3BulkFileDeletionEvent(fileUris))
            }

            scoreExposedRepository.updateSourceIdToNull(evidenceId)
            evidenceExposedRepository.deleteById(evidenceId)
        }
}
