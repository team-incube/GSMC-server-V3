package com.team.incube.gsmc.v3.domain.member.entity

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import org.jetbrains.exposed.sql.Table

object MemberExposedEntity: Table(name = "tb_member"){
    val id = integer(name = "member_id").autoIncrement()
    val name = varchar(name = "member_name", length = 25)
    val email = varchar(name = "member_email", length = 50).uniqueIndex()
    val grade = integer("member_grade").nullable()
    val classNum = integer("member_class").nullable()
    val number = integer("member_number").nullable()
    val role = enumeration<MemberRole>(name = "member_role").default(MemberRole.UNAUTHORIZED)

    override val primaryKey = PrimaryKey(id)
}
