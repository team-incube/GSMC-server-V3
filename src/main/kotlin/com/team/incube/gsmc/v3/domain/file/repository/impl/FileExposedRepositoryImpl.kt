package com.team.incube.gsmc.v3.domain.file.repository.impl

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository

@Repository
class FileExposedRepositoryImpl : FileExposedRepository {
    override fun existsByIdIn(fileIds: List<Long>): Boolean =
        FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.id inList fileIds }
            .map { it[FileExposedEntity.id] }
            .size == fileIds.size

    override fun saveFile(originalName: String, storedName: String, uri: String): File {
        val insertedId = FileExposedEntity.insert {
            it[this.originalName] = originalName
            it[this.storedName] = storedName
            it[this.uri] = uri
        } get FileExposedEntity.id

        return File(
            fileId = insertedId,
            fileOriginalName = originalName,
            fileStoredName = storedName,
            fileUri = uri
        )
    }
}
