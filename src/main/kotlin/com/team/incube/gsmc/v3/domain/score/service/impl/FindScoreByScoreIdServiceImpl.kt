package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.FindScoreByScoreIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindScoreByScoreIdServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
) : FindScoreByScoreIdService {
    override fun execute(scoreId: Long): GetScoreResponse =
        transaction {
            val score =
                scoreExposedRepository.findById(scoreId)
                    ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)

            val evidence =
                score.sourceId?.let { sourceId ->
                    evidenceExposedRepository.findById(sourceId)?.let { evidenceDto ->
                        GetEvidenceResponse (
                            evidenceId = evidenceDto.id,
                            title = evidenceDto.title,
                            content = evidenceDto.content,
                            createdAt = evidenceDto.createdAt,
                            updatedAt = evidenceDto.updatedAt,
                            files =
                                evidenceDto.files.map { file ->
                                    FileItem(
                                        fileId = file.fileId,
                                        originalName = file.fileOriginalName,
                                        storedName = file.fileStoredName,
                                        uri = file.fileUri,
                                    )
                                },
                        )
                    }
                }

            GetScoreResponse(
                scoreId = score.id!!,
                categoryNames =
                    CategoryNames(
                        koreanName = score.categoryType.koreanName,
                        englishName = score.categoryType.englishName,
                    ),
                scoreStatus = score.status,
                activityName = score.activityName,
                scoreValue = score.scoreValue,
                evidence = evidence,
                rejectionReason = score.rejectionReason,
            )
        }
}
