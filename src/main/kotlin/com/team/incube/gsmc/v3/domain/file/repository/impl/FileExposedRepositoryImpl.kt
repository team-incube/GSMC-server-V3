package com.team.incube.gsmc.v3.domain.file.repository.impl

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.EvidenceFileExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.project.entity.ProjectFileExposedEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository

@Repository
class FileExposedRepositoryImpl : FileExposedRepository {
    override fun existsById(fileId: Long): Boolean =
        FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.id eq fileId }
            .any()

    override fun existsByIdIn(fileIds: List<Long>): Boolean =
        FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.id inList fileIds }
            .map { it[FileExposedEntity.id] }
            .size == fileIds.size

    override fun saveFile(
        userId: Long,
        originalName: String,
        storedName: String,
        uri: String,
    ): File {
        val insertedId =
            FileExposedEntity.insert {
                it[this.userId] = userId
                it[this.originalName] = originalName
                it[this.storedName] = storedName
                it[this.uri] = uri
            } get FileExposedEntity.id

        return File(
            fileId = insertedId,
            userId = userId,
            fileOriginalName = originalName,
            fileStoredName = storedName,
            fileUri = uri,
        )
    }

    override fun findById(fileId: Long): File? =
        FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.id eq fileId }
            .singleOrNull()
            ?.let { row ->
                File(
                    fileId = row[FileExposedEntity.id],
                    userId = row[FileExposedEntity.userId],
                    fileOriginalName = row[FileExposedEntity.originalName],
                    fileStoredName = row[FileExposedEntity.storedName],
                    fileUri = row[FileExposedEntity.uri],
                )
            }

    override fun findAllByUserId(userId: Long): List<File> =
        FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.userId eq userId }
            .map { row ->
                File(
                    fileId = row[FileExposedEntity.id],
                    userId = row[FileExposedEntity.userId],
                    fileOriginalName = row[FileExposedEntity.originalName],
                    fileStoredName = row[FileExposedEntity.storedName],
                    fileUri = row[FileExposedEntity.uri],
                )
            }

    override fun findUnusedFilesByUserId(userId: Long): List<File> {
        val allUserFiles = findAllByUserId(userId)

        val usedFileIdsInProject =
            ProjectFileExposedEntity
                .selectAll()
                .map { it[ProjectFileExposedEntity.fileId] }
                .toSet()

        val usedFileIdsInEvidence =
            EvidenceFileExposedEntity
                .selectAll()
                .map { it[EvidenceFileExposedEntity.fileId] }
                .toSet()

        val usedFileIds = usedFileIdsInProject + usedFileIdsInEvidence

        return allUserFiles.filter { it.fileId !in usedFileIds }
    }

    override fun deleteById(fileId: Long) {
        FileExposedEntity.deleteWhere {
            FileExposedEntity.id eq fileId
        }
    }
}
