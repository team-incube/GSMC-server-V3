package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetMyScoresResponse

interface GetMyScoresService {
    fun execute(
        categoryType: CategoryType?,
        status: ScoreStatus?,
    ): GetMyScoresResponse
}
