package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectResponse

interface FindMyProjectsService {
    fun execute(): List<GetProjectResponse>
}
