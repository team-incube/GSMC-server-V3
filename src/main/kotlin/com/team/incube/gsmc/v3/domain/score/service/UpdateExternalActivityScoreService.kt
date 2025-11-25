package com.team.incube.gsmc.v3.domain.score.service

interface UpdateExternalActivityScoreService {
    fun execute(
        scoreId: Long,
        value: String,
        fileId: Long,
    )
}
