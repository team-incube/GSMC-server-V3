package com.team.incube.gsmc.v3.domain.evidence.service

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceDraftResponse

interface FindEvidenceDraftService {
    fun execute(): GetEvidenceDraftResponse?
}
