package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CreateVolunteerScoreService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateVolunteerScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
) : BaseScoreService(scoreExposedRepository, currentMemberProvider),
    CreateVolunteerScoreService {
    override fun execute(hours: Int): CreateScoreResponse =
        transaction {
            createOrUpdateScore(
                categoryType = CategoryType.VOLUNTEER,
                scoreValue = hours.toDouble(),
                sourceId = null,
                activityName = null,
            )
        }
}
