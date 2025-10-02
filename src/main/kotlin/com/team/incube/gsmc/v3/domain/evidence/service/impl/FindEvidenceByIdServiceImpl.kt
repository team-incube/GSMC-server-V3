package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceByIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FindEvidenceByIdServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
) : FindEvidenceByIdService {
    override fun execute(evidenceId: Long): GetEvidenceResponse {
        val evidence =
            evidenceExposedRepository.findById(evidenceId)
                ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)

        return GetEvidenceResponse(
            id = evidence.id,
            title = evidence.title,
            content = evidence.content,
            createdAt = evidence.createdAt,
            updatedAt = evidence.updatedAt,
            files = evidence.files,
        )
    }
}
