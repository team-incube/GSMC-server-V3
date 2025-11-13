package com.team.incube.gsmc.v3.domain.member.repository.impl

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
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
        pageable: Pageable,
    ): Page<Member> {
        val conditions =
            buildList<Op<Boolean>> {
                email?.let { add(MemberExposedEntity.email eq it) }
                name?.let { add(MemberExposedEntity.name eq it) }
                role?.let { add(MemberExposedEntity.role eq it) }
                grade?.let { add(MemberExposedEntity.grade eq it) }
                classNumber?.let { add(MemberExposedEntity.classNumber eq it) }
                number?.let { add(MemberExposedEntity.number eq it) }
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
                }.limit(pageable.pageSize)
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

    override fun searchById(memberId: Long): Member? =
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

    override fun existsByIdIn(memberIds: List<Long>): Boolean =
        MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.id inList memberIds }
            .map { it[MemberExposedEntity.id] }
            .size == memberIds.size

    override fun findByEmail(email: String): Member? =
        MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.email eq email }
            .limit(1)
            .firstOrNull()
            ?.let { row ->
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
            ?.let { row ->
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

    override fun updateMemberRoleByEmail(
        email: String,
        role: MemberRole,
    ): Int =
        MemberExposedEntity.update({ MemberExposedEntity.email eq email }) {
            it[MemberExposedEntity.role] = role
        }

    override fun deleteMemberByEmail(email: String): Int =
        MemberExposedEntity.deleteWhere { MemberExposedEntity.email eq email }
}
