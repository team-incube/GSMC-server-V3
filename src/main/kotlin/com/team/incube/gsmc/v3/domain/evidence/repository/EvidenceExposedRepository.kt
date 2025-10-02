package com.team.incube.gsmc.v3.domain.evidence.repository

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse

interface EvidenceExposedRepository {
    fun findByEvidenceId(evidenceId: Long): GetEvidenceResponse?
}
