package com.team.incube.gsmc.v3.domain.score.service

interface RejectScoreService {
    fun execute(
        scoreId: Long,
        rejectionReason: String,
    )
}