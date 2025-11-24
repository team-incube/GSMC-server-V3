package com.team.incube.gsmc.v3.domain.alert.dto

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.score.dto.Score
import java.time.LocalDateTime

data class Alert(
    val id: Long,
    val sender: Member,
    val receiver: Member,
    val score: Score,
    val alertType: AlertType,
    val isRead: Boolean,
    val content: String,
    val createdAt: LocalDateTime,
)
