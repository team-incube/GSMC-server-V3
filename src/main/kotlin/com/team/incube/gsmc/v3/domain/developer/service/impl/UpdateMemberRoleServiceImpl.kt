package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.developer.service.UpdateMemberRoleService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.config.logger
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UpdateMemberRoleServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : UpdateMemberRoleService {
    override fun execute(
        email: String,
        role: MemberRole,
    ) {
        transaction {
            val updated = memberExposedRepository.updateMemberRoleByEmail(email, role)
            if (updated == 0) {
                logger().info("Member role change failed: member not found. email={}", email)
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }
            logger().info("Member role changed successfully. email={}, newRole={}", email, role)
        }
    }
}
