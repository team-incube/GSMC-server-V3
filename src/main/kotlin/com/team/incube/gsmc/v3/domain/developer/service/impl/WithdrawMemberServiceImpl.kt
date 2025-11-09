package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.developer.repository.DeveloperExposedRepository
import com.team.incube.gsmc.v3.domain.developer.service.WithdrawMemberService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class WithdrawMemberServiceImpl(
    private val developerExposedRepository: DeveloperExposedRepository,
) : WithdrawMemberService {
    override fun execute(email: String) {
        transaction {
            val deleted = developerExposedRepository.deleteMemberByEmail(email)
            if (deleted == 0) {
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }
        }
    }
}
