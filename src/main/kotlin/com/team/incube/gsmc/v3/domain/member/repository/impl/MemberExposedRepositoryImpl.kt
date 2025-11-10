package com.team.incube.gsmc.v3.domain.member.repository.impl

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.member.presentation.data.request.SearchMemberRequest
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MemberExposedRepositoryImpl : MemberExposedRepository {
    override fun findMembers(
        condition: SearchMemberRequest,
        pageable: Pageable,
    ): Page<Member> {
        val conditions =
            buildList<Op<Boolean>> {
                condition.email?.let { add(MemberExposedEntity.email eq it) }
                condition.name?.let { add(MemberExposedEntity.name eq it) }
                condition.role?.let { add(MemberExposedEntity.role eq it) }
                condition.grade?.let { add(MemberExposedEntity.grade eq it) }
                condition.classNumber?.let { add(MemberExposedEntity.classNumber eq it) }
                condition.number?.let { add(MemberExposedEntity.number eq it) }
            }

        val whereClause = conditions.reduceOrNull { acc, condition -> acc and condition }

        val totalCount =
            MemberExposedEntity
                .selectAll()
                .apply {
                    whereClause?.let { where { it } }
                }.count()

        val members =
            MemberExposedEntity
                .selectAll()
                .apply {
                    whereClause?.let { where { it } }
                }.limit(pageable.pageSize) // Set the limit (pageSize)
                .offset(pageable.offset.toLong())
                .map { row ->
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
        return PageImpl(members, pageable, totalCount)
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
