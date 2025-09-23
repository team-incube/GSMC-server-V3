package com.team.incube.gsmc.v3.domain.evidence.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object EvidenceExposedEntity : Table(name = "tb_evidence") {
    val id = long(name = "evidence_id").autoIncrement()
    val title = varchar(name = "evidence_title", length = 100)
    val content = text(name = "evidence_content")
    val createdAt = datetime(name = "created_at").default(LocalDateTime.now())
    val updatedAt = datetime(name = "updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
