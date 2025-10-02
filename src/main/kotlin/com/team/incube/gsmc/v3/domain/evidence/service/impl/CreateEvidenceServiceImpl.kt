package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.CreateEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceService
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
) : CreateEvidenceService {
    override fun execute(
        scoreIds: List<Long>,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): CreateEvidenceResponse {
        if (!scoreExposedRepository.existsByIdIn(scoreIds)) {
            throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        }

        if (scoreExposedRepository.existsAnyWithEvidence(scoreIds)) {
            throw GsmcException(ErrorCode.SCORE_ALREADY_HAS_EVIDENCE)
        }

        if (fileIds.isNotEmpty() && !fileExposedRepository.existsByIdIn(fileIds)) {
            throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        }

        val evidence =
            evidenceExposedRepository.save(
                title = title,
                content = content,
                fileIds = fileIds,
            )

        scoreExposedRepository.updateEvidenceId(scoreIds, evidence.id)

        return CreateEvidenceResponse(
            id = evidence.id,
            title = evidence.title,
            content = evidence.content,
            createAt = evidence.createdAt,
            updateAt = evidence.updatedAt,
            file = evidence.files,
        )
    }
}
