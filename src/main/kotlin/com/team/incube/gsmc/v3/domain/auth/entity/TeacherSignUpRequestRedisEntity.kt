package com.team.incube.gsmc.v3.domain.auth.entity

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.Instant

@RedisHash("teacher_signup_request")
data class TeacherSignUpRequestRedisEntity(
    @Id
    val memberId: Long,
    val name: String,
    val email: String,
    val requestedRole: MemberRole,
    val grade: Int?,
    val classNumber: Int?,
    val requestedAt: Instant,
)
