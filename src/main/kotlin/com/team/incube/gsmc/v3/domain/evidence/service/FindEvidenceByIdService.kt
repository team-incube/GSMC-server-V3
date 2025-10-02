package com.team.incube.gsmc.v3.domain.evidence.service

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse

interface FindEvidenceByIdService {
    fun execute(evidenceId: Long): GetEvidenceResponse
}
