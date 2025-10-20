package com.team.incube.gsmc.v3.domain.score.repository

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.dto.Score

interface ScoreExposedRepository {
    fun findById(scoreId: Long): Score?

    fun existsById(scoreId: Long): Boolean

    fun existsByIdIn(scoreIds: List<Long>): Boolean

    fun updateSourceId(
        scoreIds: List<Long>,
        sourceId: Long,
    )

    fun updateStatusByScoreId(
        scoreId: Long,
        status: ScoreStatus,
    ): Int

    fun updateSourceIdToNull(sourceId: Long)

    fun existsAnyWithSource(scoreIds: List<Long>): Boolean

    fun deleteById(scoreId: Long)
}
