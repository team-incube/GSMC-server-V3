package com.team.incube.gsmc.v3.domain.project.dto

import com.team.incube.gsmc.v3.domain.file.dto.File

data class Project(
    val id: Long?,
    val ownerId: Long,
    val title: String,
    val description: String,
    val files: List<File>,
    val participants: List<ProjectParticipant>,
)
