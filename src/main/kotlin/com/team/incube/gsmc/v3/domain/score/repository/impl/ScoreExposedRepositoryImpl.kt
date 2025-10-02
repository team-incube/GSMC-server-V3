package com.team.incube.gsmc.v3.domain.score.repository.impl

import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class ScoreExposedRepositoryImpl : ScoreExposedRepository {
    override fun existsById(scoreId: Long): Boolean =
        transaction {
            ScoreExposedEntity
                .selectAll()
                .where { ScoreExposedEntity.id eq scoreId }
                .count() > 0
        }

    override fun existsByIdIn(scoreIds: List<Long>): Boolean =
        transaction {
            val existingScoreIds =
                ScoreExposedEntity
                    .selectAll()
                    .where { ScoreExposedEntity.id inList scoreIds }
                    .map { it[ScoreExposedEntity.id] }
            existingScoreIds.size == scoreIds.size
        }

    override fun updateEvidenceId(scoreIds: List<Long>, evidenceId: Long) {
        transaction {
            ScoreExposedEntity.update({ ScoreExposedEntity.id inList scoreIds }) {
                it[ScoreExposedEntity.evidenceId] = evidenceId
            }
        }
    }

    override fun existsAnyWithEvidence(scoreIds: List<Long>): Boolean =
        transaction {
            ScoreExposedEntity
                .selectAll()
                .where {
                    (ScoreExposedEntity.id inList scoreIds) and
                        ScoreExposedEntity.evidenceId.isNotNull()
                }
                .count() > 0
        }
}
