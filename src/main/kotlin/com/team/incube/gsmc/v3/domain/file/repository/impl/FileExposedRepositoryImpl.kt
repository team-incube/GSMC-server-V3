package com.team.incube.gsmc.v3.domain.file.repository.impl

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.EvidenceFileExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.project.entity.ProjectFileExposedEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
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
                it[this.member] = userId
                it[this.originalName] = originalName
                it[this.storeName] = storedName
                it[this.uri] = uri
            } get FileExposedEntity.id

        return File(
            id = insertedId,
            member = userId,
            originalName = originalName,
            storeName = storedName,
            uri = uri,
        )
    }

    override fun findById(fileId: Long): File? =
        FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.id eq fileId }
            .singleOrNull()
            ?.toFile()

    override fun findAllByUserId(userId: Long): List<File> =
        FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.member eq userId }
            .map { it.toFile() }

    override fun findUnusedFilesByUserId(userId: Long): List<File> {
        val usedInProjectSubQuery = ProjectFileExposedEntity.select(ProjectFileExposedEntity.file)
        val usedInEvidenceSubQuery = EvidenceFileExposedEntity.select(EvidenceFileExposedEntity.file)

        return FileExposedEntity
            .selectAll()
            .where {
                (FileExposedEntity.member eq userId) and
                    FileExposedEntity.id.notInSubQuery(usedInProjectSubQuery) and
                    FileExposedEntity.id.notInSubQuery(usedInEvidenceSubQuery)
            }.map { it.toFile() }
    }

    override fun deleteById(fileId: Long) {
        FileExposedEntity.deleteWhere {
            id eq fileId
        }
    }

    private fun ResultRow.toFile(): File =
        File(
            id = this[FileExposedEntity.id],
            member = this[FileExposedEntity.member],
            originalName = this[FileExposedEntity.originalName],
            storeName = this[FileExposedEntity.storeName],
            uri = this[FileExposedEntity.uri],
        )
}
