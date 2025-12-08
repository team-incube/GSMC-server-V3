package com.team.incube.gsmc.v3.domain.score.validator

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Component

@Component
class ScoreLimitValidator(
    private val scoreExposedRepository: ScoreExposedRepository,
) {
    fun validateScoreLimit(
        memberId: Long,
        categoryType: CategoryType,
    ) {
        if (scoreExposedRepository.countByMemberIdAndCategoryType(memberId, categoryType) >= categoryType.maxRecordCount) {
            throw GsmcException(ErrorCode.SCORE_MAX_LIMIT_EXCEEDED)
        }
    }
}
