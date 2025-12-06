package com.team.incube.gsmc.v3.domain.file.entity

import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import org.jetbrains.exposed.v1.core.Table

object FileExposedEntity : Table(name = "tb_file") {
    val id = long(name = "file_id").autoIncrement()
    val member = long(name = "member_id").references(MemberExposedEntity.id)
    val originalName = varchar(name = "file_original_name", length = 255)
    val storeName = varchar(name = "file_stored_name", length = 255)
    val uri = varchar(name = "file_uri", length = 512)

    override val primaryKey = PrimaryKey(id)
}
