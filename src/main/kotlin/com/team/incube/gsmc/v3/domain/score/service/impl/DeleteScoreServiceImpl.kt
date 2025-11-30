package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.DeleteScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.s3.S3BulkFileDeletionEvent
import com.team.incube.gsmc.v3.global.event.s3.S3FileDeletionEvent
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DeleteScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : DeleteScoreService {
    override fun execute(scoreId: Long) =
        transaction {
            val score =
                scoreExposedRepository.findById(scoreId)
                    ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
            score.sourceId?.let { sourceId ->
                when (score.categoryType.evidenceType) {
                    EvidenceType.EVIDENCE -> {
                        val evidence = evidenceExposedRepository.findById(sourceId)
                        evidence?.let {
                            val fileUris = it.files.map { file -> file.uri }
                            val fileIds = it.files.map { file -> file.id }
                            fileExposedRepository.deleteAllByIdIn(fileIds)
                            evidenceExposedRepository.deleteById(sourceId)
                            eventPublisher.publishEvent(S3BulkFileDeletionEvent(fileUris))
                        }
                    }
                    EvidenceType.FILE -> {
                        val file = fileExposedRepository.findById(sourceId)
                        file?.let {
                            fileExposedRepository.deleteById(it.id)
                            eventPublisher.publishEvent(S3FileDeletionEvent(it.uri))
                        }
                    }

                    EvidenceType.UNREQUIRED -> {
                    }
                }
            }
            scoreExposedRepository.deleteById(scoreId)
        }
}
