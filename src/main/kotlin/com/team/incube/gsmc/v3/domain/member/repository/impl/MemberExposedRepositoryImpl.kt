package com.team.incube.gsmc.v3.domain.member.repository.impl

import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class MemberExposedRepositoryImpl : MemberExposedRepository {
    override fun existsByIdIn(memberIds: List<Long>): Boolean =
        transaction {
            val existingMemberIds =
                MemberExposedEntity
                    .selectAll()
                    .where { MemberExposedEntity.id inList memberIds }
                    .map { it[MemberExposedEntity.id] }
            existingMemberIds.size == memberIds.size
        }
}
