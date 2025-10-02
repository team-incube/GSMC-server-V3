package com.team.incube.gsmc.v3.domain.evidence.dto

import com.team.incube.gsmc.v3.domain.file.dto.File
import java.time.Instant

data class Evidence(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val files: List<File>,
)
