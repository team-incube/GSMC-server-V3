package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse

interface CreateTopcitScoreService {
    fun execute(
        value: Int,
        fileId: Long,
    ): CreateScoreResponse
}
