package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCountBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateProjectParticipationService
import com.team.incube.gsmc.v3.domain.score.validator.ScoreLimitValidator
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateProjectParticipationServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    private val projectExposedRepository: ProjectExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
    private val scoreLimitValidator: ScoreLimitValidator,
) : BaseCountBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateProjectParticipationService {
    override fun execute(projectId: Long): CreateScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()

            val projectTitle =
                projectExposedRepository.findProjectTitleAndValidateParticipant(
                    projectId = projectId,
                    memberId = member.id,
                ) ?: run {
                    val errorCode =
                        if (projectExposedRepository.findProjectTitleById(projectId) == null) {
                            ErrorCode.PROJECT_NOT_FOUND
                        } else {
                            ErrorCode.NOT_PROJECT_PARTICIPANT
                        }
                    throw GsmcException(errorCode)
                }

            if (scoreExposedRepository.existsProjectParticipationScore(
                    memberId = member.id,
                    projectId = projectId,
                    projectTitle = projectTitle,
                )
            ) {
                throw GsmcException(ErrorCode.SCORE_ALREADY_EXISTS)
            }

            scoreLimitValidator.validateScoreLimit(member.id, CategoryType.PROJECT_PARTICIPATION)

            createScore(
                member = member,
                categoryType = CategoryType.PROJECT_PARTICIPATION,
                activityName = projectTitle,
                sourceId = null,
                status = ScoreStatus.INCOMPLETE,
            )
        }
}
