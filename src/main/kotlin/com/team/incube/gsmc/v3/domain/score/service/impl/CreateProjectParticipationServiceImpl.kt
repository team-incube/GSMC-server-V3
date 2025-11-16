package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
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
            val member = currentMemberProvider.getCurrentUser()

            val project =
                projectExposedRepository.findProjectById(projectId)
                    ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)

            val isParticipant = project.participants.any { it.id == member.id }
            if (!isParticipant) {
                throw GsmcException(ErrorCode.NOT_PROJECT_PARTICIPANT)
            }

            scoreLimitValidator.validateScoreLimit(member.id, CategoryType.PROJECT_PARTICIPATION)

            createScore(
                member = member,
                categoryType = CategoryType.PROJECT_PARTICIPATION,
                activityName = project.title,
                sourceId = projectId,
            )
        }
}
