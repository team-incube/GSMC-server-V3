package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.ScoreItem
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetMyScoresResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.GetMyScoresService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class GetMyScoresServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : GetMyScoresService {
    override fun execute(
        categoryType: CategoryType?,
        status: ScoreStatus?,
    ): GetMyScoresResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()

            val scores =
                scoreExposedRepository.findAllByMemberIdAndCategoryTypeAndStatus(
                    memberId = member.id,
                    categoryType = categoryType,
                    status = status,
                )

            GetMyScoresResponse(
                scores =
                    scores.map { score ->
                        ScoreItem(
                            scoreId = score.id!!,
                            categoryNames =
                                CategoryNames(
                                    koreanName = score.categoryType.koreanName,
                                    englishName = score.categoryType.englishName,
                                ),
                            scoreStatus = score.status,
                            activityName = score.activityName,
                            scoreValue = score.scoreValue,
                        )
                    },
            )
        }
}
