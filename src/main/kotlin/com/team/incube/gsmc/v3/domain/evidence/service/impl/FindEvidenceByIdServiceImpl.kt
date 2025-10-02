package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceByIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class FindEvidenceByIdServiceImpl(
    private val evidenceRepository: EvidenceExposedRepository,
) : FindEvidenceByIdService {
    override fun execute(evidenceId: Long): GetEvidenceResponse {
        val evidence =
            evidenceRepository.findByEvidenceId(evidenceId)
                ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)

        return GetEvidenceResponse(
            id = evidence.id,
            title = evidence.title,
            content = evidence.content,
            createdAt = LocalDateTime.ofInstant(evidence.createdAt, java.time.ZoneId.systemDefault()),
            updatedAt = LocalDateTime.ofInstant(evidence.updatedAt, java.time.ZoneId.systemDefault()),
            file = evidence.files,
        )
    }
}
