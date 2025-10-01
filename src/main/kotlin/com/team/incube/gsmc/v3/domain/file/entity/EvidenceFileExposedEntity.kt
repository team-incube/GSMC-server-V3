package com.team.incube.gsmc.v3.domain.file.entity

import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import org.jetbrains.exposed.sql.Table

object EvidenceFileExposedEntity : Table(name = "tb_evidence_file") {
    val evidenceId = long(name = "evidence_id").references(EvidenceExposedEntity.id)
    val fileId = long(name = "file_id").references(FileExposedEntity.id)

    override val primaryKey = PrimaryKey(evidenceId, fileId)
}
