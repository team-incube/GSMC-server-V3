package com.team.incube.gsmc.v3.domain.alert.service.impl

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.DeleteAlertService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteAlertServiceImpl(
    private val alertExposedRepository: AlertExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : DeleteAlertService {
    override fun execute(alertId: Long) {
        transaction {
            val member = currentMemberProvider.getCurrentMember()
            val alert =
                alertExposedRepository.findById(alertId)
                    ?: throw GsmcException(ErrorCode.ALERT_NOT_FOUND)

            if (alert.receiver.id != member.id) {
                throw GsmcException(ErrorCode.AUTHENTICATION_FAILED)
            }

            val deletedRows = alertExposedRepository.deleteById(alertId)
            if (deletedRows == 0) {
                throw GsmcException(ErrorCode.ALERT_NOT_FOUND)
            }
        }
    }
}
