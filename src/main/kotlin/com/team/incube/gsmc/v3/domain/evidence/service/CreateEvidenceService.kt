package com.team.incube.gsmc.v3.domain.evidence.service

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence

interface CreateEvidenceService {
    fun execute(
        scoreIds: List<Long>,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence
}
