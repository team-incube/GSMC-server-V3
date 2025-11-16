package com.team.incube.gsmc.v3.domain.score.repository

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.dto.Score

interface ScoreExposedRepository {
    fun findById(scoreId: Long): Score?

    fun existsById(scoreId: Long): Boolean

    fun existsByIdIn(scoreIds: List<Long>): Boolean

    fun save(score: Score): Score

    fun update(score: Score): Score

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

    fun countByMemberIdAndCategoryType(
        memberId: Long,
        categoryType: CategoryType,
    ): Long

    fun findAllByMemberId(memberId: Long): List<Score>

    fun findByMemberIdAndCategoryType(
        memberId: Long,
        categoryType: CategoryType,
    ): Score?

    fun existsByMemberIdAndCategoryTypeAndSourceId(
        memberId: Long,
        categoryType: CategoryType,
        sourceId: Long,
    ): Boolean

    fun deleteById(scoreId: Long)
}
