package com.team.incube.gsmc.v3.domain.file.entity

import org.jetbrains.exposed.sql.Table

object FileExposedEntity : Table(name = "tb_file") {
    val id = long(name = "file_id").autoIncrement()
    val originalName = varchar(name = "file_original_name", length = 255).nullable()
    val storedName = varchar(name = "file_stored_name", length = 255).nullable()
    val uri = varchar(name = "file_uri", length = 512).nullable()

    override val primaryKey = PrimaryKey(id)
}
