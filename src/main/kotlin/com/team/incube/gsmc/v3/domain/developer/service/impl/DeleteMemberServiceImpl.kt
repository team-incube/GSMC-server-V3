package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.developer.service.DeleteMemberService
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteMemberServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
) : DeleteMemberService {
    private val log = LoggerFactory.getLogger(DeleteMemberServiceImpl::class.java)

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
