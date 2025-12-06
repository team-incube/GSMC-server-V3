package com.team.incube.gsmc.v3.domain.alert.entity

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object AlertExposedEntity : Table(name = "tb_alert") {
    val id = long(name = "alert_id").autoIncrement()
    val senderId = long(name = "alert_sender_id").references(MemberExposedEntity.id)
    val receiverId = long(name = "alert_receiver_id").references(MemberExposedEntity.id)
    val scoreId = long(name = "score_id").references(ScoreExposedEntity.id)
    val alertType = enumeration<AlertType>(name = "alert_type")
    val isRead = bool(name = "alert_is_read").default(false)
    val content = varchar(name = "alert_content", length = 255).default("")
    val createdAt = timestamp(name = "alert_created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
