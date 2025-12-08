package com.team.incube.gsmc.v3.domain.evidence.repository

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence

interface EvidenceExposedRepository {
    fun findById(evidenceId: Long): Evidence?

    fun findAllByMemberId(memberId: Long): List<Evidence>

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

    fun findAllByIdIn(ids: List<Long>): List<Evidence>

    fun deleteAllByIdIn(ids: List<Long>)
}
