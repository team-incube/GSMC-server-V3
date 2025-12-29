package com.team.incube.gsmc.v3.domain.alert.service.impl

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.CreateProjectAlertService
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateProjectAlertServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
    private val alertExposedRepository: AlertExposedRepository,
) : CreateProjectAlertService {
    override fun execute(
        senderId: Long,
        receiverId: Long,
        projectId: Long,
        projectTitle: String,
        alertType: AlertType,
    ) {
        transaction {
            val sender = memberExposedRepository.findById(senderId) ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            val receiver =
                memberExposedRepository.findById(receiverId) ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)

            val content =
                when (alertType) {
                    AlertType.PROJECT_INVITATION -> {
                        "${sender.name} 학생이 프로젝트 \"$projectTitle\"에 당신을 초대했습니다."
                    }

                    else -> {
                        throw GsmcException(ErrorCode.INVALID_ALERT_TYPE)
                    }
                }

            alertExposedRepository.saveWithoutScore(sender, receiver, alertType, content)
        }
    }
}
