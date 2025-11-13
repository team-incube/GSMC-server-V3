package com.team.incube.gsmc.v3.domain.score.dto

import com.team.incube.gsmc.v3.domain.category.dto.Category
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.member.dto.Member

data class Score(
    val id: Long,
    val member: Member,
    val category: Category,
    val status: ScoreStatus,
    val sourceId: Long?,
    val activityName: String?,
)
