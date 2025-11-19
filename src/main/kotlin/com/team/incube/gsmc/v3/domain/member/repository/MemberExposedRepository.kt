package com.team.incube.gsmc.v3.domain.member.repository

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.dto.constant.SortDirection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface MemberExposedRepository {
    fun searchMembers(
        email: String?,
        name: String?,
        role: MemberRole?,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        sortBy: SortDirection?,
        pageable: Pageable,
    ): Page<Member>

    fun findByEmail(email: String): Member?

    fun save(
        name: String,
        email: String,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        role: MemberRole,
    ): Member

    fun findById(id: Long): Member?

    fun updateMemberRoleByEmail(
        email: String,
        role: MemberRole,
    ): Int

    fun deleteMemberByEmail(email: String): Int

    fun update(
        id: Long,
        name: String,
        email: String,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        role: MemberRole,
    ): Int
}
