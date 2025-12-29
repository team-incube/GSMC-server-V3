package com.team.incube.gsmc.v3.global.event.alert

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType

data class CreateProjectAlertEvent(
    val senderId: Long,
    val receiverIds: List<Long>,
    val projectId: Long,
    val projectTitle: String,
    val alertType: AlertType,
)
