package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceByIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindEvidenceByIdServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
) : FindEvidenceByIdService {
    override fun execute(evidenceId: Long): GetEvidenceResponse =
        transaction {
            val evidence =
                evidenceExposedRepository.findById(evidenceId)
                    ?: throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)

            GetEvidenceResponse(
                evidenceId = evidence.id,
                title = evidence.title,
                content = evidence.content,
                createdAt = evidence.createdAt,
                updatedAt = evidence.updatedAt,
                files = evidence.files,
            )
        }
}
