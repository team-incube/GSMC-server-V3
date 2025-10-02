package com.team.incube.gsmc.v3.domain.file.repository.impl

import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class FileExposedRepositoryImpl : FileExposedRepository {
    override fun existsByIdIn(fileIds: List<Long>): Boolean =
        transaction {
            val existingFileIds =
                FileExposedEntity
                    .selectAll()
                    .where { FileExposedEntity.id inList fileIds }
                    .map { it[FileExposedEntity.id] }
            existingFileIds.size == fileIds.size
        }
}
