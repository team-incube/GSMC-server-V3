package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.ApproveScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.alert.CreateAlertEvent
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class ApproveScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val currentMemberProvider: CurrentMemberProvider,
) : ApproveScoreService {
    override fun execute(scoreId: Long) {
        transaction {
            val score =
                scoreExposedRepository.findById(scoreId)
                    ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)

            scoreExposedRepository.updateStatusAndRejectionReasonByScoreId(
                scoreId = scoreId,
                status = ScoreStatus.APPROVED,
                rejectionReason = null,
            )

            val member = currentMemberProvider.getCurrentMember()
            eventPublisher.publishEvent(
                CreateAlertEvent(
                    senderId = member.id,
                    receiverId = score.member.id,
                    scoreId = scoreId,
                    alertType = AlertType.APPROVED,
                ),
            )
        }
    }
}
