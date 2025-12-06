package com.team.incube.gsmc.v3.domain.project.entity

import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object ProjectScoreExposedEntity : Table(name = "tb_project_score") {
    val project =
        long(name = "project_id")
            .references(ProjectExposedEntity.id, onDelete = ReferenceOption.CASCADE)
    val score =
        long(name = "score_id")
            .references(ScoreExposedEntity.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(project, score)
}
