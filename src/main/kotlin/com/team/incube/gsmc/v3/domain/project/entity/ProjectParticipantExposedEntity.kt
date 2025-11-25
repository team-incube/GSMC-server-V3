package com.team.incube.gsmc.v3.domain.project.entity

import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import org.jetbrains.exposed.sql.Table

object ProjectParticipantExposedEntity : Table(name = "tb_project_participant") {
    val project = long(name = "project_id").references(ProjectExposedEntity.id)
    val member = long(name = "member_id").references(MemberExposedEntity.id)

    override val primaryKey = PrimaryKey(project, member)
}
