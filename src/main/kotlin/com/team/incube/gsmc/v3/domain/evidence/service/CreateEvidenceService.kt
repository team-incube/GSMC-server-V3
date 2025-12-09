package com.team.incube.gsmc.v3.domain.evidence.service

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.CreateEvidenceResponse

interface CreateEvidenceService {
    fun execute(
        scoreId: Long,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): CreateEvidenceResponse
}
