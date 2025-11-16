package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.dto.Project

interface FindProjectByIdService {
    fun execute(projectId: Long): Project
}