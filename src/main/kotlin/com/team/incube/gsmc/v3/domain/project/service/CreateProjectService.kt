package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.ProjectResponse

interface CreateProjectService {
    fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): ProjectResponse
}
