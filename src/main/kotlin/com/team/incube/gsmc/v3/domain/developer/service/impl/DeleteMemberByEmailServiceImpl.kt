package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.developer.service.DeleteMemberByEmailService
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteMemberByEmailServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : DeleteMemberByEmailService {
    private val log = LoggerFactory.getLogger(DeleteMemberByEmailServiceImpl::class.java)

    override fun execute(email: String) {
        transaction {
            val deleted = memberExposedRepository.deleteMemberByEmail(email)

            if (deleted == 0) {
                log.info("Member withdrawal failed: member not found. email={}", email)
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }

            log.info("Member withdrawn successfully. email={}", email)
        }
    }
}
