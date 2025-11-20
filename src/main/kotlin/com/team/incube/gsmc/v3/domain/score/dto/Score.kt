package com.team.incube.gsmc.v3.domain.score.dto

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.member.dto.Member

data class Score(
    val id: Long?,
    val member: Member,
    val categoryType: CategoryType,
    val status: ScoreStatus,
    val sourceId: Long?,
    val activityName: String?,
    val scoreValue: Double?,
    val rejectionReason: String?,
)
