package com.team.incube.gsmc.v3.domain.score.repository.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class ScoreExposedRepositoryImpl : ScoreExposedRepository {
    override fun findById(scoreId: Long): Score? =
        ScoreExposedEntity
            .join(MemberExposedEntity, joinType = JoinType.INNER) {
                ScoreExposedEntity.memberId eq MemberExposedEntity.id
            }.selectAll()
            .where { ScoreExposedEntity.id eq scoreId }
            .map { row ->
                val member =
                    Member(
                        id = row[MemberExposedEntity.id],
                        name = row[MemberExposedEntity.name],
                        email = row[MemberExposedEntity.email],
                        grade = row[MemberExposedEntity.grade],
                        classNumber = row[MemberExposedEntity.classNumber],
                        number = row[MemberExposedEntity.number],
                        role = row[MemberExposedEntity.role],
                    )

                val categoryType = CategoryType.fromEnglishName(row[ScoreExposedEntity.categoryEnglishName])

                Score(
                    id = row[ScoreExposedEntity.id],
                    member = member,
                    categoryType = categoryType,
                    status = row[ScoreExposedEntity.status],
                    sourceId = row[ScoreExposedEntity.sourceId],
                    activityName = row[ScoreExposedEntity.activityName],
                    scoreValue = row[ScoreExposedEntity.scoreValue],
                )
            }.singleOrNull()

    override fun existsById(scoreId: Long): Boolean =
        ScoreExposedEntity
            .selectAll()
            .where { ScoreExposedEntity.id eq scoreId }
            .count() > 0

    override fun existsByIdIn(scoreIds: List<Long>): Boolean {
        val existingScoreIds =
            ScoreExposedEntity
                .selectAll()
                .where { ScoreExposedEntity.id inList scoreIds }
                .map { it[ScoreExposedEntity.id] }
        return existingScoreIds.size == scoreIds.size
    }

    override fun save(score: Score): Score {
        val generatedId =
            ScoreExposedEntity.insert {
                it[memberId] = score.member.id
                it[categoryEnglishName] = score.categoryType.englishName
                it[status] = score.status
                it[sourceId] = score.sourceId
                it[activityName] = score.activityName
                it[scoreValue] = score.scoreValue
            } get ScoreExposedEntity.id
        return score.copy(id = generatedId)
    }

    override fun updateSourceId(
        scoreIds: List<Long>,
        sourceId: Long,
    ) {
        ScoreExposedEntity.update({ ScoreExposedEntity.id inList scoreIds }) {
            it[ScoreExposedEntity.sourceId] = sourceId
        }
    }

    override fun updateSourceIdToNull(sourceId: Long) {
        ScoreExposedEntity.update({ ScoreExposedEntity.sourceId eq sourceId }) {
            it[ScoreExposedEntity.sourceId] = null
        }
    }

    override fun updateStatusByScoreId(
        scoreId: Long,
        status: ScoreStatus,
    ): Int =
        ScoreExposedEntity.update({ ScoreExposedEntity.id eq scoreId }) {
            it[ScoreExposedEntity.status] = status
        }

    override fun existsAnyWithSource(scoreIds: List<Long>): Boolean =
        ScoreExposedEntity
            .selectAll()
            .where {
                (ScoreExposedEntity.id inList scoreIds) and
                    ScoreExposedEntity.sourceId.isNotNull()
            }.count() > 0

    override fun countByMemberIdAndCategoryType(
        memberId: Long,
        categoryType: CategoryType,
    ): Long =
        ScoreExposedEntity
            .selectAll()
            .where {
                (ScoreExposedEntity.memberId eq memberId) and
                    (ScoreExposedEntity.categoryEnglishName eq categoryType.englishName)
            }.count()

    override fun findAllByMemberId(memberId: Long): List<Score> =
        ScoreExposedEntity
            .join(MemberExposedEntity, joinType = JoinType.INNER) {
                ScoreExposedEntity.memberId eq MemberExposedEntity.id
            }.selectAll()
            .where { ScoreExposedEntity.memberId eq memberId }
            .map { row ->
                val member =
                    Member(
                        id = row[MemberExposedEntity.id],
                        name = row[MemberExposedEntity.name],
                        email = row[MemberExposedEntity.email],
                        grade = row[MemberExposedEntity.grade],
                        classNumber = row[MemberExposedEntity.classNumber],
                        number = row[MemberExposedEntity.number],
                        role = row[MemberExposedEntity.role],
                    )

                val categoryType = CategoryType.fromEnglishName(row[ScoreExposedEntity.categoryEnglishName])

                Score(
                    id = row[ScoreExposedEntity.id],
                    member = member,
                    categoryType = categoryType,
                    status = row[ScoreExposedEntity.status],
                    sourceId = row[ScoreExposedEntity.sourceId],
                    activityName = row[ScoreExposedEntity.activityName],
                    scoreValue = row[ScoreExposedEntity.scoreValue],
                )
            }

    override fun deleteById(scoreId: Long) {
        ScoreExposedEntity.deleteWhere { ScoreExposedEntity.id eq scoreId }
    }
}
