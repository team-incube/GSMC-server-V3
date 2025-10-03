package com.team.incube.gsmc.v3.domain.evidence.service

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.PatchEvidenceResponse

interface UpdateEvidenceService {
    fun execute(
        evidenceId: Long,
        participants: List<Long>?,
        title: String?,
        content: String?,
        fileIds: List<Long>?,
    ): PatchEvidenceResponse
}
