package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCreateOrUpdateBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateVolunteerScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.alert.CreateAlertEvent
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class CreateVolunteerScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
    private val eventPublisher: ApplicationEventPublisher,
    private val memberExposedRepository: MemberExposedRepository,
) : BaseCreateOrUpdateBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateVolunteerScoreService {
    override fun execute(
        value: String,
        memberId: Long,
    ): CreateScoreResponse =
        transaction {
            val intValue =
                value.toIntOrNull()
                    ?: throw GsmcException(ErrorCode.SCORE_INVALID_VALUE)

            if (intValue < 1) {
                throw GsmcException(ErrorCode.SCORE_VALUE_OUT_OF_RANGE)
            }

            val student =
                memberExposedRepository.findById(memberId)
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)

            val score =
                createOrUpdateScore(
                    member = student,
                    categoryType = CategoryType.VOLUNTEER,
                    scoreValue = intValue.toDouble(),
                    sourceId = null,
                )

            val teacher = currentMemberProvider.getCurrentMember()

            eventPublisher.publishEvent(
                CreateAlertEvent(
                    senderId = teacher.id,
                    receiverId = student.id,
                    scoreId = score.scoreId,
                    alertType = AlertType.ADD_SCORE,
                ),
            )
            score
        }
}
