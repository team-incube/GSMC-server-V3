package com.team.incube.gsmc.v3.domain.evidence.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object EvidenceExposedEntity : Table(name = "tb_evidence") {
    val id = long(name = "evidence_id").autoIncrement()
    val title = varchar(name = "evidence_title", length = 255)
    val content = text(name = "evidence_content")
    val createdAt = timestamp(name = "evidence_created_at").default(Instant.now())
    val updatedAt = timestamp(name = "evidence_updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
