package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.DeleteEvidenceService
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
) : DeleteEvidenceService {
    override fun execute(evidenceId: Long) =
        transaction {
            evidenceExposedRepository.findById(evidenceId)
                ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)
            scoreExposedRepository.updateSourceIdToNull(evidenceId)
            evidenceExposedRepository.deleteById(evidenceId)
        }
}
