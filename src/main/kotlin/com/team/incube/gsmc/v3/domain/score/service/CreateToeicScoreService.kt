package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse

interface CreateToeicScoreService {
    fun execute(
        value: String,
        fileId: Long,
    ): CreateScoreResponse
}
