package com.team.incube.gsmc.v3.domain.evidence.repository

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence

interface EvidenceExposedRepository {
    fun findById(evidenceId: Long): Evidence?

    fun save(
        userId: Long,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence

    fun update(
        id: Long,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence

    fun deleteById(evidenceId: Long)
}
