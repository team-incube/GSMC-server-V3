package com.team.incube.gsmc.v3.domain.evidence.repository

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence

interface EvidenceExposedRepository {
    fun findByEvidenceId(evidenceId: Long): Evidence?
}
