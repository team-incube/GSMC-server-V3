package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.CreateEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceService
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
) : CreateEvidenceService {
    override fun execute(
        scoreId: Long,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): CreateEvidenceResponse =
        transaction {
            val score =
                scoreExposedRepository.findById(scoreId)
                    ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)

            if (scoreExposedRepository.existsWithSource(scoreId)) {
                throw GsmcException(ErrorCode.SCORE_ALREADY_HAS_EVIDENCE)
            }

            if (fileIds.isNotEmpty() && !fileExposedRepository.existsByIdIn(fileIds)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            val evidence =
                evidenceExposedRepository.save(
                    userId = currentMemberProvider.getCurrentMember().id,
                    title = title,
                    content = content,
                    fileIds = fileIds,
                )

            scoreExposedRepository.updateSourceId(scoreId, evidence.id)

            if (score.status == ScoreStatus.INCOMPLETE) {
                scoreExposedRepository.updateStatusByScoreId(scoreId, ScoreStatus.PENDING)
            }

            CreateEvidenceResponse(
                id = evidence.id,
                title = evidence.title,
                content = evidence.content,
                createAt = evidence.createdAt,
                updateAt = evidence.updatedAt,
                files =
                    evidence.files.map { file ->
                        GetFileResponse(
                            id = file.id,
                            originalName = file.originalName,
                            storeName = file.storeName,
                            uri = file.uri,
                            memberId = file.member,
                        )
                    },
            )
        }
}
