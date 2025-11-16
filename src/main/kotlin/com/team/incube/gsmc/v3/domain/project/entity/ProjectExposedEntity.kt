package com.team.incube.gsmc.v3.domain.project.entity

import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import org.jetbrains.exposed.sql.Table

object ProjectExposedEntity : Table(name = "tb_project") {
    val id = long(name = "project_id").autoIncrement()
    val ownerId = long(name = "owner_id").references(MemberExposedEntity.id)
    val title = varchar(name = "project_title", length = 255)
    val description = text(name = "project_description")

    override val primaryKey = PrimaryKey(id)
}
