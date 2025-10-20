package com.team.incube.gsmc.v3.domain.score.repository.impl

import com.team.incube.gsmc.v3.domain.category.dto.Category
import com.team.incube.gsmc.v3.domain.category.entity.CategoryExposedEntity
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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class ScoreExposedRepositoryImpl : ScoreExposedRepository {
    override fun findById(scoreId: Long): Score? =
        ScoreExposedEntity
            .join(MemberExposedEntity, joinType = JoinType.INNER) {
                ScoreExposedEntity.memberId eq MemberExposedEntity.id
            }.join(CategoryExposedEntity, joinType = JoinType.INNER) {
                ScoreExposedEntity.categoryId eq CategoryExposedEntity.id
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

                val category =
                    Category(
                        id = row[CategoryExposedEntity.id],
                        englishName = row[CategoryExposedEntity.englishName],
                        koreanName = row[CategoryExposedEntity.koreanName],
                        weight = row[CategoryExposedEntity.weight],
                        maximumValue = row[CategoryExposedEntity.maximumValue],
                        isAccumulated = row[CategoryExposedEntity.isAccumulated],
                        evidenceType = row[CategoryExposedEntity.evidenceType],
                    )

                val sourceId = row[ScoreExposedEntity.sourceId]

                Score(
                    id = row[ScoreExposedEntity.id],
                    member = member,
                    category = category,
                    status = row[ScoreExposedEntity.status],
                    sourceId = sourceId,
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

    override fun deleteById(scoreId: Long) {
        ScoreExposedEntity.deleteWhere { ScoreExposedEntity.id eq scoreId }
    }
}
