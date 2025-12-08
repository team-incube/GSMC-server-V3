package com.team.incube.gsmc.v3.global.event.alert

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType

data class CreateAlertEvent(
    val senderId: Long,
    val receiverId: Long,
    val scoreId: Long,
    val alertType: AlertType,
)
