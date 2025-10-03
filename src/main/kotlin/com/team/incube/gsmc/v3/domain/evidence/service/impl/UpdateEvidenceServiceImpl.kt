package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.PatchEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.UpdateEvidenceService
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
) : UpdateEvidenceService {
    override fun execute(
        evidenceId: Long,
        participants: List<Long>?,
        title: String?,
        content: String?,
        fileIds: List<Long>?,
    ): PatchEvidenceResponse {
        val evidence = evidenceExposedRepository.findById(evidenceId)
            ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)

        // 파일 ID 검증
        if (!fileIds.isNullOrEmpty() && !fileExposedRepository.existsByIdIn(fileIds)) {
            throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        }

        // 참가자 검증 (participants가 scoreIds를 의미한다고 가정)
        if (!participants.isNullOrEmpty()) {
            if (!scoreExposedRepository.existsByIdIn(participants)) {
                throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
            }

            // 기존 연결된 점수들의 증빙자료 연결 해제
            scoreExposedRepository.updateEvidenceIdToNull(evidenceId)

            // 새로운 점수들과 연결
            scoreExposedRepository.updateEvidenceId(participants, evidenceId)
        }

        // 증빙자료 업데이트
        val updatedEvidence = evidenceExposedRepository.update(
            id = evidenceId,
            title = title ?: evidence.title,
            content = content ?: evidence.content,
            fileIds = fileIds ?: evidence.files.map { it.fileId!! }
        )

        return PatchEvidenceResponse(
            id = updatedEvidence.id,
            title = updatedEvidence.title,
            content = updatedEvidence.content,
            createAt = updatedEvidence.createdAt,
            updateAt = updatedEvidence.updatedAt,
            file = updatedEvidence.files,
        )
    }
}
