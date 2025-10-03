package com.team.incube.gsmc.v3.domain.evidence.repository.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.EvidenceFileExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneOffset

@Repository
class EvidenceExposedRepositoryImpl : EvidenceExposedRepository {
    override fun findById(evidenceId: Long): Evidence? {
        return transaction {
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
                createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                files = files,
            )
        }
    }

    override fun save(
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence =
        transaction {
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
                        .map { row ->
                            File(
                                fileId = row[FileExposedEntity.id],
                                fileOriginalName = row[FileExposedEntity.originalName],
                                fileStoredName = row[FileExposedEntity.storedName],
                                fileUri = row[FileExposedEntity.uri],
                            )
                        }
                } else {
                    emptyList()
                }

            Evidence(
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
    ): Evidence =
        transaction {
            val now = Instant.now()

            // 증빙자료 정보 업데이트
            EvidenceExposedEntity.update({ EvidenceExposedEntity.id eq id }) {
                it[this.title] = title
                it[this.content] = content
                it[updatedAt] = now
            }

            // 기존 파일 연결 삭제
            EvidenceFileExposedEntity.deleteWhere { evidenceId eq id }

            // 새로운 파일 연결 추가
            if (fileIds.isNotEmpty()) {
                EvidenceFileExposedEntity.batchInsert(fileIds) { fileId ->
                    this[EvidenceFileExposedEntity.evidenceId] = id
                    this[EvidenceFileExposedEntity.fileId] = fileId
                }
            }

            // 업데이트된 증빙자료 조회
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
                createdAt = firstRow[EvidenceExposedEntity.createdAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                updatedAt = firstRow[EvidenceExposedEntity.updatedAt].atOffset(ZoneOffset.UTC).toLocalDateTime(),
                files = files,
            )
        }
}
