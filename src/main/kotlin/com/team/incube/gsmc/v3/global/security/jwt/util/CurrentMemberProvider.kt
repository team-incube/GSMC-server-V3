package com.team.incube.gsmc.v3.global.security.jwt.util

import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class CurrentMemberProvider(
    private val memberExposedRepository: MemberExposedRepository,
) {
    fun getCurrentUser(): Member {
        val principal = SecurityContextHolder.getContext().authentication.principal

        if (principal is Long) {
            return memberExposedRepository.findById(principal)
                ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
        }

        throw GsmcException(ErrorCode.AUTHENTICATION_FAILED)
    }
}
