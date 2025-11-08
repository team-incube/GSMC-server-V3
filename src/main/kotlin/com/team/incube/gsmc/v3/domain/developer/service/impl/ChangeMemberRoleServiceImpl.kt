package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.developer.repository.DeveloperExposedRepository
import com.team.incube.gsmc.v3.domain.developer.service.ChangeMemberRoleService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ChangeMemberRoleServiceImpl(
    private val developerExposedRepository: DeveloperExposedRepository,
) : ChangeMemberRoleService {

    override fun execute(
        email: String,
        role: MemberRole,
    ) {
        transaction {
            val updated = developerExposedRepository.updateMemberRoleByEmail(email, role)
            if (updated == 0) {
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }
        }
    }
}
