package com.team.incube.gsmc.v3.domain.alert.service.impl

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.PatchAlertIsReadService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class PatchAlertIsReadServiceImpl(
    private val alertExposedRepository: AlertExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : PatchAlertIsReadService {
    override fun execute(lastAlertId: Long) {
        transaction {
            val currentMember = currentMemberProvider.getCurrentMember()

            alertExposedRepository.updateIsReadTrueByReceiverIdAndLastAlertId(
                receiverId = currentMember.id,
                lastAlertId = lastAlertId
            )
        }
    }
}
