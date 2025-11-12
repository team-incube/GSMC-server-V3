package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.CreateEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceService
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
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
    ): CreateEvidenceResponse =
        transaction {
            if (!scoreExposedRepository.existsByIdIn(scoreIds)) {
                throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
            }

            if (scoreExposedRepository.existsAnyWithSource(scoreIds)) {
                throw GsmcException(ErrorCode.SCORE_ALREADY_HAS_EVIDENCE)
            }

            if (fileIds.isNotEmpty() && !fileExposedRepository.existsByIdIn(fileIds)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            val evidence =
                evidenceExposedRepository.save(
                    // TODO: security context에서 userId 받아오기
                    userId = 0L,
                    title = title,
                    content = content,
                    fileIds = fileIds,
                )

            scoreExposedRepository.updateSourceId(scoreIds, evidence.id)

            CreateEvidenceResponse(
                id = evidence.id,
                title = evidence.title,
                content = evidence.content,
                createAt = evidence.createdAt,
                updateAt = evidence.updatedAt,
                file = evidence.files,
            )
        }
}
