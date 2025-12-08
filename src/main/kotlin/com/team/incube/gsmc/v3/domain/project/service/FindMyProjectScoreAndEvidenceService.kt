package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetMyProjectScoreAndEvidenceResponse

interface FindMyProjectScoreAndEvidenceService {
    fun execute(projectId: Long): GetMyProjectScoreAndEvidenceResponse
}
