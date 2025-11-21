package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoresByCategoryResponse

interface FindScoresByCategoryService {
    fun execute(status: ScoreStatus?): GetScoresByCategoryResponse
}
