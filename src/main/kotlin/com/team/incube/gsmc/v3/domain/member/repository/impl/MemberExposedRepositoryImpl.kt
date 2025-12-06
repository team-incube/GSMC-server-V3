package com.team.incube.gsmc.v3.domain.member.repository.impl

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.dto.constant.SortDirection
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MemberExposedRepositoryImpl : MemberExposedRepository {
    override fun searchMembers(
        email: String?,
        name: String?,
        role: MemberRole?,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        sortBy: SortDirection?,
        pageable: Pageable,
    ): Page<Member> {
        val conditions =
            buildList {
                email?.let { add(MemberExposedEntity.email like "%$it%") }
                name?.let { add(MemberExposedEntity.name like "%$it%") }
                role?.let { add(MemberExposedEntity.role eq it) }
                grade?.let { add(MemberExposedEntity.grade eq it) }
                classNumber?.let { add(MemberExposedEntity.classNumber eq it) }
                number?.let { add(MemberExposedEntity.number eq it) }
            }

        val whereClause = conditions.reduceOrNull { acc, condition -> acc and condition }

        val totalCount =
            MemberExposedEntity
                .selectAll()
                .apply { whereClause?.let { where { it } } }
                .count()

        val sortOrder = if (sortBy == SortDirection.DESC) SortOrder.DESC else SortOrder.ASC

        val members =
            MemberExposedEntity
                .selectAll()
                .apply { whereClause?.let { where { it } } }
                .orderBy(MemberExposedEntity.grade to sortOrder)
                .orderBy(MemberExposedEntity.classNumber to sortOrder)
                .orderBy(MemberExposedEntity.number to sortOrder)
                .limit(pageable.pageSize)
                .offset(pageable.offset)
                .map { it.toMember() }

        return PageImpl(members, pageable, totalCount)
    }

    override fun findByEmail(email: String): Member? =
        MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.email eq email }
            .limit(1)
            .firstOrNull()
            ?.toMember()

    override fun save(
        name: String,
        email: String,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        role: MemberRole,
    ): Member {
        val id =
            MemberExposedEntity.insert { st ->
                st[MemberExposedEntity.name] = name
                st[MemberExposedEntity.email] = email
                st[MemberExposedEntity.grade] = grade
                st[MemberExposedEntity.classNumber] = classNumber
                st[MemberExposedEntity.number] = number
                st[MemberExposedEntity.role] = role
            } get MemberExposedEntity.id

        return Member(
            id = id,
            name = name,
            email = email,
            grade = grade,
            classNumber = classNumber,
            number = number,
            role = role,
        )
    }

    override fun findById(id: Long): Member? =
        MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toMember()

    override fun findAllByIdIn(ids: List<Long>): List<Member> =
        MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.id inList ids }
            .map { it.toMember() }

    override fun existsById(id: Long): Boolean =
        MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.id eq id }
            .empty()
            .not()

    override fun updateMemberRoleByEmail(
        email: String,
        role: MemberRole,
    ): Int =
        MemberExposedEntity.update({ MemberExposedEntity.email eq email }) {
            it[MemberExposedEntity.role] = role
        }

    override fun deleteMemberByEmail(email: String): Int = MemberExposedEntity.deleteWhere { MemberExposedEntity.email eq email }

    override fun update(
        id: Long,
        name: String,
        email: String,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        role: MemberRole,
    ): Int =
        MemberExposedEntity.update({ MemberExposedEntity.id eq id }) {
            it[this.name] = name
            it[this.email] = email
            it[this.grade] = grade
            it[this.classNumber] = classNumber
            it[this.number] = number
            it[this.role] = role
        }

    private fun ResultRow.toMember(): Member =
        Member(
            id = this[MemberExposedEntity.id],
            name = this[MemberExposedEntity.name],
            email = this[MemberExposedEntity.email],
            grade = this[MemberExposedEntity.grade],
            classNumber = this[MemberExposedEntity.classNumber],
            number = this[MemberExposedEntity.number],
            role = this[MemberExposedEntity.role],
        )
}
