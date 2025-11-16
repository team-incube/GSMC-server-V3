package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.dto.Project

interface CreateCurrentProjectService {
    fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project
}
