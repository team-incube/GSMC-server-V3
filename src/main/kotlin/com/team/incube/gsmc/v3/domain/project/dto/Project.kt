package com.team.incube.gsmc.v3.domain.project.dto

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.member.dto.Member

data class Project(
    val id: Long?,
    val ownerId: Long,
    val title: String,
    val description: String,
    val files: List<File>,
    val participants: List<Member>,
)
