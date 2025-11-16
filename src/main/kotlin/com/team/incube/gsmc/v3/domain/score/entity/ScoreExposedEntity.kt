package com.team.incube.gsmc.v3.domain.score.entity

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Table

object ScoreExposedEntity : Table(name = "tb_score") {
    val id = long(name = "score_id").autoIncrement()
    val memberId = long(name = "member_id").references(MemberExposedEntity.id)
    val categoryEnglishName = varchar(name = "category_english_name", length = 100)
    val status = enumeration<ScoreStatus>(name = "score_status").default(ScoreStatus.PENDING)
    val sourceId = long(name = "source_id").nullable()
    val activityName = varchar(name = "activity_name", length = 255).nullable()
    val scoreValue = double(name = "score_value").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(
            customIndexName = "uk_score_member_category_source",
            isUnique = true,
            columns = arrayOf(memberId, categoryEnglishName, sourceId),
        )
    }
}
