package com.team.incube.gsmc.v3.domain.developer.repository.impl

import com.team.incube.gsmc.v3.domain.developer.repository.DeveloperExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class DeveloperExposedRepositoryImpl : DeveloperExposedRepository {

    override fun updateMemberRoleByEmail(
        email: String,
        role: MemberRole,
    ): Int =
        MemberExposedEntity.update({ MemberExposedEntity.email eq email }) {
            it[MemberExposedEntity.role] = role
        }

    override fun deleteMemberByEmail(email: String): Int =
        MemberExposedEntity.deleteWhere {
            MemberExposedEntity.email eq email
        }
}
