package com.team.incube.gsmc.v3.domain.project.entity

import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import org.jetbrains.exposed.sql.Table

object ProjectFileExposedEntity : Table(name = "tb_project_file") {
    val projectId = long(name = "project_id").references(ProjectExposedEntity.id)
    val fileId = long(name = "file_id").references(FileExposedEntity.id)

    override val primaryKey = PrimaryKey(projectId, fileId)
}