package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoreResponse

interface FindScoreByScoreIdService {
    fun execute(scoreId: Long): GetScoreResponse
}