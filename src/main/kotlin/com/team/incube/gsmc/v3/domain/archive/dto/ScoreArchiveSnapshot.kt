package com.team.incube.gsmc.v3.domain.archive.dto

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import java.time.Instant

data class ScoreArchiveSnapshot(
    val scoreId: Long,
    val status: ScoreStatus,
    val sourceId: Long?,
    val activityName: String?,
    val scoreValue: Double?,
    val rejectionReason: String?,
    val updatedAt: Instant?,
)
