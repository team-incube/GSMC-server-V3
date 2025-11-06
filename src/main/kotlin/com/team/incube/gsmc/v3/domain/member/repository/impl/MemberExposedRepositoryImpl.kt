package com.team.incube.gsmc.v3.domain.member.repository.impl

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.member.presentation.data.request.SearchMemberRequest
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.springframework.stereotype.Repository

@Repository
class MemberExposedRepositoryImpl : MemberExposedRepository {
    override fun findMembers(query: SearchMemberRequest): List<Member> {
        val conditions =
            buildList<Op<Boolean>> {
                query.email?.let { add(MemberExposedEntity.email eq it) }
                query.name?.let { add(MemberExposedEntity.name eq it) }
                query.role?.let { add(MemberExposedEntity.role eq it) }
                query.grade?.let { add(MemberExposedEntity.grade eq it) }
                query.classNumber?.let { add(MemberExposedEntity.classNumber eq it) }
                query.number?.let { add(MemberExposedEntity.number eq it) }
            }

        val whereClause = conditions.reduceOrNull { acc, condition -> acc and condition }

        val queryResult =
            MemberExposedEntity.selectAll().apply {
                whereClause?.let { where { it } }
            }
        return queryResult.map { row ->
            Member(
                id = row[MemberExposedEntity.id],
                name = row[MemberExposedEntity.name],
                email = row[MemberExposedEntity.email],
                grade = row[MemberExposedEntity.grade],
                classNumber = row[MemberExposedEntity.classNumber],
                number = row[MemberExposedEntity.number],
                role = row[MemberExposedEntity.role],
            )
        }
    }

    override fun findById(memberId: Long): Member? =
        MemberExposedEntity
            .selectAll()
            .where {
                MemberExposedEntity.id eq memberId
            }.map { row ->
                Member(
                    id = row[MemberExposedEntity.id],
                    name = row[MemberExposedEntity.name],
                    email = row[MemberExposedEntity.email],
                    grade = row[MemberExposedEntity.grade],
                    classNumber = row[MemberExposedEntity.classNumber],
                    number = row[MemberExposedEntity.number],
                    role = row[MemberExposedEntity.role],
                )
            }.singleOrNull()
}
