package com.team.incube.gsmc.v3.domain.member.entity

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import org.jetbrains.exposed.sql.Table

object MemberExposedEntity : Table(name = "tb_member") {
    val id = long(name = "member_id").autoIncrement()
    val name = varchar(name = "member_name", length = 25)
    val email = varchar(name = "member_email", length = 50)
    val grade = integer(name = "member_grade").nullable()
    val classNumber = integer(name = "member_class_number").nullable()
    val number = integer(name = "member_number").nullable()
    val role = enumeration<MemberRole>(name = "member_role").default(MemberRole.UNAUTHORIZED)

    override val primaryKey = PrimaryKey(id)
}
