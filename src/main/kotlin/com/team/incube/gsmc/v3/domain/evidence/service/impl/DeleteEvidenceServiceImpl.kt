package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.DeleteEvidenceService
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DeleteEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
) : DeleteEvidenceService {
    override fun execute(evidenceId: Long) {
        // 증빙자료 존재 여부 확인
        evidenceExposedRepository.findById(evidenceId)
            ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)

        // 연결된 점수들의 증빙자료 연결 해제
        scoreExposedRepository.updateEvidenceIdToNull(evidenceId)

        // 증빙자료 삭제
        evidenceExposedRepository.deleteById(evidenceId)
    }
}
