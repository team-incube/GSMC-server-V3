package com.team.incube.gsmc.v3.domain.score.entity

import com.team.incube.gsmc.v3.domain.category.entity.CategoryExposedEntity
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import org.jetbrains.exposed.sql.Table

object ScoreExposedEntity : Table(name = "tb_score") {
    val id = long(name = "score_id").autoIncrement()
    val memberId = long(name = "member_id").references(MemberExposedEntity.id)
    val categoryId = long(name = "category_id").references(CategoryExposedEntity.id)
    val status = enumeration<ScoreStatus>(name = "score_status").default(ScoreStatus.PENDING)
    val evidenceId = long(name = "evidence_id").references(EvidenceExposedEntity.id).nullable()

    override val primaryKey = PrimaryKey(id, memberId, categoryId)
}
