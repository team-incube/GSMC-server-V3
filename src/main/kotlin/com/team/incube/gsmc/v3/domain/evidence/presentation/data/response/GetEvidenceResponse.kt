package com.team.incube.gsmc.v3.domain.evidence.presentation.data.response

import com.team.incube.gsmc.v3.domain.file.dto.File
import java.time.LocalDateTime

data class GetEvidenceResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val file: List<File>,
)
