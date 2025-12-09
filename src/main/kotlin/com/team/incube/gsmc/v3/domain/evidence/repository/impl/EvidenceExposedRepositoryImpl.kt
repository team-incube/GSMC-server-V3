package com.team.incube.gsmc.v3.domain.evidence.repository.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.EvidenceFileExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
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
                .mapNotNull { it.toFile() }
                .distinctBy { it.id }
        return Evidence(
            id = firstRow[EvidenceExposedEntity.id],
            member = firstRow[EvidenceExposedEntity.member],
            title = firstRow[EvidenceExposedEntity.title],
            content = firstRow[EvidenceExposedEntity.content],
            createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            files = files,
        )
    }

    override fun findAllByMemberId(memberId: Long): List<Evidence> {
        val rows =
            EvidenceExposedEntity
                .leftJoin(EvidenceFileExposedEntity)
                .leftJoin(FileExposedEntity)
                .selectAll()
                .where { EvidenceExposedEntity.member eq memberId }

        return rows
            .groupBy { it[EvidenceExposedEntity.id] }
            .map { (_, evidenceRows) ->
                val firstRow = evidenceRows.first()
                val files =
                    evidenceRows
                        .mapNotNull { it.toFile() }
                        .distinctBy { it.id }
                Evidence(
                    id = firstRow[EvidenceExposedEntity.id],
                    member = firstRow[EvidenceExposedEntity.member],
                    title = firstRow[EvidenceExposedEntity.title],
                    content = firstRow[EvidenceExposedEntity.content],
                    createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                    updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                    files = files,
                )
            }
    }

    override fun save(
        userId: Long,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence {
        val now = Instant.now()

        val evidenceId =
            EvidenceExposedEntity.insert {
                it[this.member] = userId
                it[this.title] = title
                it[this.content] = content
                it[createdAt] = now
                it[updatedAt] = now
            } get EvidenceExposedEntity.id

        if (fileIds.isNotEmpty()) {
            EvidenceFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[EvidenceFileExposedEntity.evidence] = evidenceId
                this[EvidenceFileExposedEntity.file] = fileId
            }
        }

        val files =
            if (fileIds.isNotEmpty()) {
                FileExposedEntity
                    .selectAll()
                    .where { FileExposedEntity.id inList fileIds }
                    .mapNotNull { it.toFile() }
            } else {
                emptyList()
            }

        return Evidence(
            id = evidenceId,
            member = userId,
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

        EvidenceFileExposedEntity.deleteWhere { evidence eq id }

        if (fileIds.isNotEmpty()) {
            EvidenceFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[EvidenceFileExposedEntity.evidence] = id
                this[EvidenceFileExposedEntity.file] = fileId
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
                .mapNotNull { it.toFile() }
                .distinctBy { it.id }

        return Evidence(
            id = firstRow[EvidenceExposedEntity.id],
            member = firstRow[EvidenceExposedEntity.member],
            title = firstRow[EvidenceExposedEntity.title],
            content = firstRow[EvidenceExposedEntity.content],
            createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
            files = files,
        )
    }

    override fun deleteById(evidenceId: Long) {
        EvidenceFileExposedEntity.deleteWhere { EvidenceFileExposedEntity.evidence eq evidenceId }
        EvidenceExposedEntity.deleteWhere { id eq evidenceId }
    }

    override fun findAllByIdIn(ids: List<Long>): List<Evidence> {
        if (ids.isEmpty()) return emptyList()

        val rows =
            EvidenceExposedEntity
                .leftJoin(EvidenceFileExposedEntity)
                .leftJoin(FileExposedEntity)
                .selectAll()
                .where { EvidenceExposedEntity.id inList ids }

        return rows
            .groupBy { it[EvidenceExposedEntity.id] }
            .map { (_, evidenceRows) ->
                val firstRow = evidenceRows.first()
                val files =
                    evidenceRows
                        .mapNotNull { it.toFile() }
                        .distinctBy { it.id }
                Evidence(
                    id = firstRow[EvidenceExposedEntity.id],
                    member = firstRow[EvidenceExposedEntity.member],
                    title = firstRow[EvidenceExposedEntity.title],
                    content = firstRow[EvidenceExposedEntity.content],
                    createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                    updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                    files = files,
                )
            }
    }

    override fun deleteAllByIdIn(ids: List<Long>) {
        if (ids.isEmpty()) return
        EvidenceFileExposedEntity.deleteWhere { EvidenceFileExposedEntity.evidence inList ids }
        EvidenceExposedEntity.deleteWhere { id inList ids }
    }

    private fun ResultRow.toFile(): File? {
        val id = this.getOrNull(FileExposedEntity.id)
        val member = this.getOrNull(FileExposedEntity.member)
        val originalName = this.getOrNull(FileExposedEntity.originalName)
        val storedName = this.getOrNull(FileExposedEntity.storeName)
        val uri = this.getOrNull(FileExposedEntity.uri)

        return if (id != null && member != null && originalName != null && storedName != null && uri != null) {
            File(
                id = id,
                member = member,
                originalName = originalName,
                storeName = storedName,
                uri = uri,
            )
        } else {
            null
        }
    }
}
