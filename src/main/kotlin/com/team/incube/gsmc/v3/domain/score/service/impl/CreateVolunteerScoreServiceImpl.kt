package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CreateVolunteerScoreService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateVolunteerScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : CreateVolunteerScoreService {
    override fun execute(hours: Int): CreateScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()

            val existingScore =
                scoreExposedRepository.findByMemberIdAndCategoryType(
                    memberId = member.id,
                    categoryType = CategoryType.VOLUNTEER,
                )

            val savedScore =
                if (existingScore != null) {
                    scoreExposedRepository.update(
                        existingScore.copy(
                            status = ScoreStatus.PENDING,
                            scoreValue = hours,
                        ),
                    )
                } else {
                    scoreExposedRepository.save(
                        Score(
                            id = null,
                            member = member,
                            categoryType = CategoryType.VOLUNTEER,
                            status = ScoreStatus.PENDING,
                            sourceId = null,
                            activityName = null,
                            scoreValue = hours,
                        ),
                    )
                }

            CreateScoreResponse(
                scoreId = savedScore.id!!,
                categoryNames =
                    CategoryNames(
                        koreanName = savedScore.categoryType.koreanName,
                        englishName = savedScore.categoryType.englishName,
                    ),
                scoreStatus = savedScore.status,
                sourceId = savedScore.sourceId,
                activityName = savedScore.activityName,
            )
        }
}
