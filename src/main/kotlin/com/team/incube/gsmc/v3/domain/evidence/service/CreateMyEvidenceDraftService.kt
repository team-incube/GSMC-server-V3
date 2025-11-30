package com.team.incube.gsmc.v3.domain.evidence.service

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.CreateEvidenceDraftRequest
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceDraftResponse

interface CreateMyEvidenceDraftService {
    fun execute(request: CreateEvidenceDraftRequest): GetEvidenceDraftResponse
}
