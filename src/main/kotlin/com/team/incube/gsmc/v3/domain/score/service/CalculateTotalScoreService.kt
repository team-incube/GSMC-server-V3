package com.team.incube.gsmc.v3.domain.score.service

interface CalculateTotalScoreService {
    fun execute(
        memberId: Long,
        includeApprovedOnly: Boolean,
    ): Int
}
