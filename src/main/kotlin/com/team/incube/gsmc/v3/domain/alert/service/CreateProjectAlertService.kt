package com.team.incube.gsmc.v3.domain.alert.service

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType

interface CreateProjectAlertService {
    fun execute(
        senderId: Long,
        receiverIds: List<Long>,
        projectId: Long,
        projectTitle: String,
        alertType: AlertType,
    )
}
