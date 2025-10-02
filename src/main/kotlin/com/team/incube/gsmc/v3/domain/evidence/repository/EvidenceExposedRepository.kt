package com.team.incube.gsmc.v3.domain.evidence.repository

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence

interface EvidenceExposedRepository {
    fun findById(evidenceId: Long): Evidence?

    fun save(
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence
}
