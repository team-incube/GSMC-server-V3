package com.team.incube.gsmc.v3.domain.alert.service.impl

import com.team.incube.gsmc.v3.domain.alert.presentation.data.response.GetAlertResponse
import com.team.incube.gsmc.v3.domain.alert.presentation.data.response.GetMyAlertsResponse
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.FindMyAlertsService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyAlertsServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val alertExposedRepository: AlertExposedRepository,
) : FindMyAlertsService {
    override fun execute(): GetMyAlertsResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()

            val alerts = alertExposedRepository.findAllByReceiverId(receiverId = member.id)

            GetMyAlertsResponse(
                alerts =
                    alerts.map { alert ->
                        GetAlertResponse(
                            id = alert.id,
                            title = alert.alertType.title,
                            content = alert.content,
                            createdAt = alert.createdAt,
                            alertType = alert.alertType,
                            scoreId = alert.score.id!!,
                        )
                    },
            )
        }
}
