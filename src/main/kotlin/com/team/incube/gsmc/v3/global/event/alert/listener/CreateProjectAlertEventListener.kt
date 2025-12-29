package com.team.incube.gsmc.v3.global.event.alert.listener

import com.team.incube.gsmc.v3.domain.alert.service.CreateProjectAlertService
import com.team.incube.gsmc.v3.global.event.alert.CreateProjectAlertEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CreateProjectAlertEventListener(
    private val createProjectAlertService: CreateProjectAlertService,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCreateProjectAlert(event: CreateProjectAlertEvent) {
        event.receiverIds.forEach { receiverId ->
            createProjectAlertService.execute(
                senderId = event.senderId,
                receiverId = receiverId,
                projectId = event.projectId,
                projectTitle = event.projectTitle,
                alertType = event.alertType,
            )
        }
    }
}
