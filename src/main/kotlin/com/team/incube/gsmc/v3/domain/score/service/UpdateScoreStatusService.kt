package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus

interface UpdateScoreStatusService {
    fun execute(
        scoreId: Long,
        scoreStatus: ScoreStatus,
    )
}
