package com.team.incube.gsmc.v3.domain.score.repository

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus

interface ScoreExposedRepository {
    fun findById(scoreId: Long): Score?

    fun existsById(scoreId: Long): Boolean

    fun existsByIdIn(scoreIds: List<Long>): Boolean

    fun save(score: Score): Score

    fun update(score: Score): Score

    fun updateSourceId(
        scoreId: Long,
        sourceId: Long,
    )

    fun updateStatusByScoreId(
        scoreId: Long,
        status: ScoreStatus,
    ): Int

    fun updateStatusAndRejectionReasonByScoreId(
        scoreId: Long,
        status: ScoreStatus,
        rejectionReason: String?,
    ): Int

    fun updateSourceIdToNull(sourceId: Long)

    fun existsWithSource(scoreId: Long): Boolean

    fun countByMemberIdAndCategoryType(
        memberId: Long,
        categoryType: CategoryType,
    ): Long

    fun findAllByMemberId(memberId: Long): List<Score>

    fun findByMemberIdAndStatus(
        memberId: Long,
        status: ScoreStatus,
    ): List<Score>

    fun findByMemberIdsAndStatus(
        memberIds: List<Long>,
        status: ScoreStatus,
    ): List<Score>

    fun findAllByStatus(status: ScoreStatus): List<Score>

    fun findByMemberIdAndCategoryType(
        memberId: Long,
        categoryType: CategoryType,
    ): Score?

    fun deleteByIdIn(scoreIds: List<Long>)
    
    fun findByMemberIdAndCategoryTypeAndStatus(
        memberId: Long,
        categoryType: CategoryType?,
        status: ScoreStatus?,
    ): List<Score>

    fun existsByMemberIdAndCategoryTypeAndSourceId(
        memberId: Long,
        categoryType: CategoryType,
        sourceId: Long,
    ): Boolean

    fun findProjectParticipationScore(
        memberId: Long,
        projectId: Long,
    ): Score?

    fun existsProjectParticipationScore(
        memberId: Long,
        projectId: Long,
    ): Boolean

    fun updateActivityName(
        scoreId: Long,
        activityName: String,
    )

    fun updateActivityNameByIdIn(
        scoreIds: List<Long>,
        activityName: String,
    )

    fun findAllByIdIn(scoreIds: List<Long>): List<Score>

    fun findAllByActivityName(activityName: String): List<Score>

    fun findAllByActivityNameAndCategoryType(
        activityName: String,
        categoryType: CategoryType,
    ): List<Score>

    fun deleteById(scoreId: Long)

    fun deleteAllByIdIn(scoreIds: List<Long>)
}
