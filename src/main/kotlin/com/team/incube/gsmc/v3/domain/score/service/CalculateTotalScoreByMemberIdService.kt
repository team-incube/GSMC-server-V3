package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetTotalScoreResponse

interface CalculateTotalScoreByMemberIdService {
    fun execute(
        memberId: Long,
        includeApprovedOnly: Boolean,
    ): GetTotalScoreResponse
}