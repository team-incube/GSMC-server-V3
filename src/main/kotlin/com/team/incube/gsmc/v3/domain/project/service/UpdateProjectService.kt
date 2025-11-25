package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.ProjectResponse

interface UpdateProjectService {
    fun execute(
        projectId: Long,
        title: String?,
        description: String?,
        fileIds: List<Long>?,
        participantIds: List<Long>?,
    ): ProjectResponse
}
