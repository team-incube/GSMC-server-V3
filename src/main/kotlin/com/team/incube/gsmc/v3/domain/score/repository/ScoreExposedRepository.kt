package com.team.incube.gsmc.v3.domain.score.repository

interface ScoreExposedRepository {
    fun existsById(scoreId: Long): Boolean
    fun existsByIdIn(scoreIds: List<Long>): Boolean
    fun updateEvidenceId(scoreIds: List<Long>, evidenceId: Long)
    fun existsAnyWithEvidence(scoreIds: List<Long>): Boolean
}
