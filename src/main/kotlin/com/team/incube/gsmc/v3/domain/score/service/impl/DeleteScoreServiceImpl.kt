package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.DeleteScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val s3DeleteService: S3DeleteService,
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
                        evidence?.files?.forEach { file ->
                            s3DeleteService.execute(file.fileUri)
                            fileExposedRepository.deleteById(file.fileId)
                        }
                        evidenceExposedRepository.deleteById(sourceId)
                    }

                    EvidenceType.FILE -> {
                        val file = fileExposedRepository.findById(sourceId)
                        file?.let {
                            s3DeleteService.execute(it.fileUri)
                            fileExposedRepository.deleteById(it.fileId)
                        }
                    }

                    EvidenceType.UNREQUIRED -> {
                    }
                }
            }
            scoreExposedRepository.deleteById(scoreId)
        }
}
