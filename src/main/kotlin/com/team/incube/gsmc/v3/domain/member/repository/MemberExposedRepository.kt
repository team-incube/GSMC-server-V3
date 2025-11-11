package com.team.incube.gsmc.v3.domain.member.repository

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
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
        pageable: Pageable,
    ): Page<Member>

    fun searchById(memberId: Long): Member?

    fun existsByIdIn(memberIds: List<Long>): Boolean

    fun updateMemberRoleByEmail(
        email: String,
        role: MemberRole,
    ): Int

    fun deleteMemberByEmail(email: String): Int
}
