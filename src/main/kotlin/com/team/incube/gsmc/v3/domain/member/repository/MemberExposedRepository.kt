package com.team.incube.gsmc.v3.domain.member.repository

interface MemberExposedRepository {
    fun existsByIdIn(memberIds: List<Long>): Boolean
}
