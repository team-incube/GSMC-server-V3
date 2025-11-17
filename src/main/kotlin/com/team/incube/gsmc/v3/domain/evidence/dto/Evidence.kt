package com.team.incube.gsmc.v3.domain.evidence.dto

import com.team.incube.gsmc.v3.domain.file.dto.File
import java.time.LocalDateTime

data class Evidence(
    val id: Long,
    val memberId: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val files: List<File>,
)
