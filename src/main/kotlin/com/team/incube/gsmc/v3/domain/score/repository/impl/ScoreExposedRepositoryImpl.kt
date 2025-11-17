package com.team.incube.gsmc.v3.domain.score.repository.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
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
                val member = row.toMember()
                row.toScore(member)
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

    override fun update(score: Score): Score {
        ScoreExposedEntity.update({ ScoreExposedEntity.id eq score.id!! }) {
            it[status] = score.status
            it[sourceId] = score.sourceId
            it[activityName] = score.activityName
            it[scoreValue] = score.scoreValue
        }
        return score
    }

    override fun updateSourceId(
        scoreId: Long,
        sourceId: Long,
    ) {
        ScoreExposedEntity.update({ ScoreExposedEntity.id eq scoreId }) {
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

    override fun existsWithSource(scoreId: Long): Boolean =
        !ScoreExposedEntity
            .select(ScoreExposedEntity.id)
            .where {
                (ScoreExposedEntity.id eq scoreId) and
                    ScoreExposedEntity.sourceId.isNotNull()
            }.limit(1)
            .empty()

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

    override fun findAllByMemberId(memberId: Long): List<Score> {
        val results =
            ScoreExposedEntity
                .join(MemberExposedEntity, joinType = JoinType.INNER) {
                    ScoreExposedEntity.memberId eq MemberExposedEntity.id
                }.selectAll()
                .where { ScoreExposedEntity.memberId eq memberId }
                .toList()

        if (results.isEmpty()) return emptyList()

        val member = results.first().toMember()

        return results.map { it.toScore(member) }
    }

    override fun findByMemberIdAndCategoryType(
        memberId: Long,
        categoryType: CategoryType,
    ): Score? =
        ScoreExposedEntity
            .join(MemberExposedEntity, joinType = JoinType.INNER) {
                ScoreExposedEntity.memberId eq MemberExposedEntity.id
            }.selectAll()
            .where {
                (ScoreExposedEntity.memberId eq memberId) and
                    (ScoreExposedEntity.categoryEnglishName eq categoryType.englishName)
            }.map { row ->
                val member = row.toMember()
                row.toScore(member)
            }.singleOrNull()

    override fun findAllByMemberIdAndCategoryTypeAndStatus(
        memberId: Long,
        categoryType: CategoryType?,
        status: ScoreStatus?,
    ): List<Score> {
        var query =
            ScoreExposedEntity
                .join(MemberExposedEntity, joinType = JoinType.INNER) {
                    ScoreExposedEntity.memberId eq MemberExposedEntity.id
                }.selectAll()
                .where { ScoreExposedEntity.memberId eq memberId }

        categoryType?.let {
            query = query.andWhere { ScoreExposedEntity.categoryEnglishName eq it.englishName }
        }

        status?.let {
            query = query.andWhere { ScoreExposedEntity.status eq it }
        }

        val results = query.toList()

        if (results.isEmpty()) return emptyList()

        val member = results.first().toMember()

        return results.map { it.toScore(member) }
    }

    override fun existsByMemberIdAndCategoryTypeAndSourceId(
        memberId: Long,
        categoryType: CategoryType,
        sourceId: Long,
    ): Boolean =
        !ScoreExposedEntity
            .select(ScoreExposedEntity.id)
            .where {
                (ScoreExposedEntity.memberId eq memberId) and
                    (ScoreExposedEntity.categoryEnglishName eq categoryType.englishName) and
                    (ScoreExposedEntity.sourceId eq sourceId)
            }.limit(1)
            .empty()

    override fun deleteById(scoreId: Long) {
        ScoreExposedEntity.deleteWhere { ScoreExposedEntity.id eq scoreId }
    }

    private fun ResultRow.toMember(): Member =
        Member(
            id = this[MemberExposedEntity.id],
            name = this[MemberExposedEntity.name],
            email = this[MemberExposedEntity.email],
            grade = this[MemberExposedEntity.grade],
            classNumber = this[MemberExposedEntity.classNumber],
            number = this[MemberExposedEntity.number],
            role = this[MemberExposedEntity.role],
        )

    private fun ResultRow.toScore(member: Member): Score {
        val categoryType = CategoryType.fromEnglishName(this[ScoreExposedEntity.categoryEnglishName])

        return Score(
            id = this[ScoreExposedEntity.id],
            member = member,
            categoryType = categoryType,
            status = this[ScoreExposedEntity.status],
            sourceId = this[ScoreExposedEntity.sourceId],
            activityName = this[ScoreExposedEntity.activityName],
            scoreValue = this[ScoreExposedEntity.scoreValue],
        )
    }
}
