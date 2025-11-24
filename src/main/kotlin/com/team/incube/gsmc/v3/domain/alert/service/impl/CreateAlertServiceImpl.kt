package com.team.incube.gsmc.v3.domain.alert.service.impl

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.alert.service.CreateAlertService
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateAlertServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
    private val alertExposedRepository: AlertExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
) : CreateAlertService {
    companion object {
        private const val ADD_SCORE_TEMPLATE = "%s 점수를 %s 학생이 등록하였습니다."
        private const val REJECTED_TEMPLATE = "%s 점수를 %s 선생님께서 거부하셨습니다."
        private const val APPROVED_TEMPLATE = "%s 점수를 %s 선생님께서 통과시키셨습니다."
    }

    override fun execute(
        senderId: Long,
        receiverId: Long,
        scoreId: Long,
        alertType: AlertType,
    ) {
        transaction {
            val sender = memberExposedRepository.findById(senderId) ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            val receiver =
                memberExposedRepository.findById(receiverId) ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)

            val score = scoreExposedRepository.findById(scoreId) ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)

            val content =
                when (alertType) {
                    AlertType.ADD_SCORE ->
                        String.format(ADD_SCORE_TEMPLATE, score.activityName, sender.name)

                    AlertType.REJECTED ->
                        String.format(REJECTED_TEMPLATE, score.activityName, receiver.name)

                    AlertType.APPROVED ->
                        String.format(APPROVED_TEMPLATE, score.activityName, receiver.name)
                }

            alertExposedRepository.save(sender, receiver, score, alertType, content)
        }
    }
}
