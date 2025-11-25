package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.UpdateScoreResponse

interface UpdateAwardScoreService {
    fun execute(
        scoreId: Long,
        value: String,
        fileId: Long,
    ): UpdateScoreResponse
}
