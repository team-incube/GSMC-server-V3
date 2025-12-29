package com.team.incube.gsmc.v3.global.event.alert.listener

import com.team.incube.gsmc.v3.domain.alert.service.CreateAlertService
import com.team.incube.gsmc.v3.global.event.alert.CreateScoreAlertEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CreateScoreAlertEventListener(
    private val createAlertService: CreateAlertService,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCreateScoreAlert(event: CreateScoreAlertEvent) {
        createAlertService.execute(
            senderId = event.senderId,
            receiverId = event.receiverId,
            scoreId = event.scoreId,
            alertType = event.alertType,
        )
    }
}
