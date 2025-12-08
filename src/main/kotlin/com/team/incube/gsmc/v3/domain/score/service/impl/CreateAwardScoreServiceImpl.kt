package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCountBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateAwardScoreService
import com.team.incube.gsmc.v3.domain.score.validator.ScoreLimitValidator
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.alert.CreateAlertEvent
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class CreateAwardScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
    private val memberExposedRepository: MemberExposedRepository,
    private val scoreLimitValidator: ScoreLimitValidator,
    private val eventPublisher: ApplicationEventPublisher,
) : BaseCountBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateAwardScoreService {
    override fun execute(
        value: String,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()
            if (!fileExposedRepository.existsById(fileId)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }
            scoreLimitValidator.validateScoreLimit(member.id, CategoryType.AWARD)

            val score =
                createScore(
                    member = member,
                    categoryType = CategoryType.AWARD,
                    activityName = value,
                    sourceId = fileId,
                )

            val grade = member.grade
            val classNumber = member.classNumber

            if (grade != null && classNumber != null) {
                val homeroomTeacher =
                    memberExposedRepository
                        .findByGradeAndClassNumberAndRole(
                            grade = grade,
                            classNumber = classNumber,
                            role = MemberRole.HOMEROOM_TEACHER,
                        ).firstOrNull()

                homeroomTeacher?.let { teacher ->
                    eventPublisher.publishEvent(
                        CreateAlertEvent(
                            senderId = member.id,
                            receiverId = teacher.id,
                            scoreId = score.scoreId,
                            alertType = AlertType.ADD_SCORE,
                        ),
                    )
                }
            }
            score
        }
}
