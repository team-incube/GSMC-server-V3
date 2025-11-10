package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.developer.service.UpdateMemberRoleService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UpdateMemberRoleServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : UpdateMemberRoleService {
    private val log = LoggerFactory.getLogger(UpdateMemberRoleServiceImpl::class.java)

    override fun execute(
        email: String,
        role: MemberRole,
    ) {
        transaction {
            val updated = memberExposedRepository.updateMemberRoleByEmail(email, role)

            if (updated == 0) {
                log.info("Member role change failed: member not found. email={}", email)
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }

            log.info("Member role changed successfully. email={}, newRole={}", email, role)
        }
    }
}
