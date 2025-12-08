package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.presentation.data.request.CreateProjectDraftRequest
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectDraftResponse

interface CreateMyProjectDraftService {
    fun execute(request: CreateProjectDraftRequest): GetProjectDraftResponse
}
