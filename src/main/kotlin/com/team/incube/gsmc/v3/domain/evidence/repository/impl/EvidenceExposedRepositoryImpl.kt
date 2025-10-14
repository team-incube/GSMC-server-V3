package com.team.incube.gsmc.v3.domain.evidence.repository.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.EvidenceFileExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset

@Repository
class EvidenceExposedRepositoryImpl : EvidenceExposedRepository {
    override fun findById(evidenceId: Long): Evidence? {
        val rows =
            EvidenceExposedEntity
                .leftJoin(EvidenceFileExposedEntity)
                .leftJoin(FileExposedEntity)
                .selectAll()
                .where { EvidenceExposedEntity.id eq evidenceId }
        if (rows.empty()) {
            return null
        }
        val firstRow = rows.first()
        val files =
            rows
                .mapNotNull { row ->
                    val fileId = row.getOrNull(FileExposedEntity.id)
                    val originalName = row.getOrNull(FileExposedEntity.originalName)
                    val storedName = row.getOrNull(FileExposedEntity.storedName)
                    val uri = row.getOrNull(FileExposedEntity.uri)
                    if (fileId != null && originalName != null && storedName != null && uri != null) {
                        File(
                            fileId = fileId,
                            fileOriginalName = originalName,
                            fileStoredName = storedName,
                            fileUri = uri,
                        )
                    } else {
                        null
                    }
                }.distinctBy { it.fileId }
        return Evidence(
            id = firstRow[EvidenceExposedEntity.id],
            title = firstRow[EvidenceExposedEntity.title],
            content = firstRow[EvidenceExposedEntity.content],
            createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            files = files,
        )
    }

    override fun save(
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence {
        val now = Instant.now()

        val evidenceId =
            EvidenceExposedEntity.insert {
                it[this.title] = title
                it[this.content] = content
                it[createdAt] = now
                it[updatedAt] = now
            } get EvidenceExposedEntity.id

        if (fileIds.isNotEmpty()) {
            EvidenceFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[EvidenceFileExposedEntity.evidenceId] = evidenceId
                this[EvidenceFileExposedEntity.fileId] = fileId
            }
        }

        val files =
            if (fileIds.isNotEmpty()) {
                FileExposedEntity
                    .selectAll()
                    .where { FileExposedEntity.id inList fileIds }
                    .mapNotNull { row ->
                        val originalName = row.getOrNull(FileExposedEntity.originalName)
                        val storedName = row.getOrNull(FileExposedEntity.storedName)
                        val uri = row.getOrNull(FileExposedEntity.uri)
                        if (originalName != null && storedName != null && uri != null) {
                            File(
                                fileId = row[FileExposedEntity.id],
                                fileOriginalName = originalName,
                                fileStoredName = storedName,
                                fileUri = uri,
                            )
                        } else {
                            null
                        }
                    }
            } else {
                emptyList()
            }

        return Evidence(
            id = evidenceId,
            title = title,
            content = content,
            createdAt = now.atOffset(ZoneOffset.UTC).toLocalDateTime(),
            updatedAt = now.atOffset(ZoneOffset.UTC).toLocalDateTime(),
            files = files,
        )
    }

    override fun update(
        id: Long,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence {
        val now = Instant.now()

        EvidenceExposedEntity.update({ EvidenceExposedEntity.id eq id }) {
            it[this.title] = title
            it[this.content] = content
            it[updatedAt] = now
        }

        EvidenceFileExposedEntity.deleteWhere { EvidenceFileExposedEntity.evidenceId eq id }

        if (fileIds.isNotEmpty()) {
            EvidenceFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[EvidenceFileExposedEntity.evidenceId] = id
                this[EvidenceFileExposedEntity.fileId] = fileId
            }
        }

        val rows =
            EvidenceExposedEntity
                .leftJoin(EvidenceFileExposedEntity)
                .leftJoin(FileExposedEntity)
                .selectAll()
                .where { EvidenceExposedEntity.id eq id }

        val firstRow = rows.first()
        val files =
            rows
                .mapNotNull { row ->
                    val fileId = row.getOrNull(FileExposedEntity.id)
                    val originalName = row.getOrNull(FileExposedEntity.originalName)
                    val storedName = row.getOrNull(FileExposedEntity.storedName)
                    val uri = row.getOrNull(FileExposedEntity.uri)
                    if (fileId != null && originalName != null && storedName != null && uri != null) {
                        File(
                            fileId = fileId,
                            fileOriginalName = originalName,
                            fileStoredName = storedName,
                            fileUri = uri,
                        )
                    } else {
                        null
                    }
                }.distinctBy { it.fileId }

        return Evidence(
            id = firstRow[EvidenceExposedEntity.id],
            title = firstRow[EvidenceExposedEntity.title],
            content = firstRow[EvidenceExposedEntity.content],
            createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            files = files,
        )
    }

    override fun deleteById(evidenceId: Long) {
        EvidenceFileExposedEntity.deleteWhere { EvidenceFileExposedEntity.evidenceId eq evidenceId }
        EvidenceExposedEntity.deleteWhere { EvidenceExposedEntity.id eq evidenceId }
    }
}
