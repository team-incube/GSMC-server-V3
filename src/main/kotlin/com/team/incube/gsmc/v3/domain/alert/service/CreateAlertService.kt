package com.team.incube.gsmc.v3.domain.alert.service

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType

interface CreateAlertService {
    fun execute(
        senderId: Long,
        receiverId: Long,
        scoreId: Long,
        alertType: AlertType,
    )
}
