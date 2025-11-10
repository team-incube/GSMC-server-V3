package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.developer.repository.DeveloperExposedRepository
import com.team.incube.gsmc.v3.domain.developer.service.WithdrawMemberService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WithdrawMemberServiceImpl(
    private val developerExposedRepository: DeveloperExposedRepository,
) : WithdrawMemberService {
    private val log = LoggerFactory.getLogger(WithdrawMemberServiceImpl::class.java)

    override fun execute(email: String) {
        transaction {
            val deleted = developerExposedRepository.deleteMemberByEmail(email)

            if (deleted == 0) {
                log.info("Developer member withdrawal failed: member not found. email={}", email)
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }

            log.info("Developer member withdrawn successfully. email={}", email)
        }
    }
}
