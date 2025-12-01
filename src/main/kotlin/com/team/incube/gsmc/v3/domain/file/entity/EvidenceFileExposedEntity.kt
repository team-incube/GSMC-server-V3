package com.team.incube.gsmc.v3.domain.file.entity

import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import org.jetbrains.exposed.sql.Table

object EvidenceFileExposedEntity : Table(name = "tb_evidence_file") {
    val evidence = long(name = "evidence_id").references(EvidenceExposedEntity.id)
    val file = long(name = "file_id").references(FileExposedEntity.id)

    override val primaryKey = PrimaryKey(evidence, file)
}
