package com.team.incube.gsmc.v3.domain.alert.repository

import com.team.incube.gsmc.v3.domain.alert.dto.Alert
import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.score.dto.Score

interface AlertExposedRepository {
    fun findById(alertId: Long): Alert?

    fun deleteById(alertId: Long): Int

    fun findAllByReceiverId(receiverId: Long): List<Alert>

    fun save(
        sender: Member,
        receiver: Member,
        score: Score,
        alertType: AlertType,
        content: String,
    ): Alert

    fun updateIsReadTrueByReceiverIdAndLastAlertId(
        receiverId: Long,
        lastAlertId: Long,
    ): Int
}
