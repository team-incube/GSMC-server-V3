package com.team.incube.gsmc.v3.domain.score.service

interface UpdateAwardScoreService {
    fun execute(
        scoreId: Long,
        value: String,
        fileId: Long,
    )
}
