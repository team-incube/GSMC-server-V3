package com.team.incube.gsmc.v3.domain.evidence.repository.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.EvidenceFileExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class EvidenceExposedRepositoryImpl : EvidenceExposedRepository {
    override fun findByEvidenceId(evidenceId: Long): Evidence? =
        transaction {
            val rows =
                EvidenceExposedEntity
                    .leftJoin(EvidenceFileExposedEntity)
                    .leftJoin(FileExposedEntity)
                    .selectAll()
                    .where { EvidenceExposedEntity.id eq evidenceId }
            if (rows.empty()) {
                return@transaction null
            }
            val firstRow = rows.first()
            val files =
                rows
                    .mapNotNull { row ->
                        val fileId = row.getOrNull(FileExposedEntity.id)
                        if (fileId != null) {
                            File(
                                fileId = fileId,
                                fileOriginalName = row.getOrNull(FileExposedEntity.originalName),
                                fileStoredName = row.getOrNull(FileExposedEntity.storedName),
                                fileUri = row.getOrNull(FileExposedEntity.uri),
                            )
                        } else {
                            null
                        }
                    }.distinctBy { it.fileId }
            Evidence(
                id = firstRow[EvidenceExposedEntity.id],
                title = firstRow[EvidenceExposedEntity.title],
                content = firstRow[EvidenceExposedEntity.content],
                createdAt = firstRow[EvidenceExposedEntity.createdAt],
                updatedAt = firstRow[EvidenceExposedEntity.updatedAt],
                files = files,
            )
        }
}
