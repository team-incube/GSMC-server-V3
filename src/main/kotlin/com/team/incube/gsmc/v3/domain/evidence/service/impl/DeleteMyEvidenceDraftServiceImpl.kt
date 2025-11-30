package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceDraftRedisRepository
import com.team.incube.gsmc.v3.domain.evidence.service.DeleteMyEvidenceDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.stereotype.Service

@Service
class DeleteMyEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val evidenceDraftRedisRepository: EvidenceDraftRedisRepository,
) : DeleteMyEvidenceDraftService {
    override fun execute() {
        val memberId = currentMemberProvider.getCurrentMemberId()
        evidenceDraftRedisRepository.deleteById(memberId)
    }
}
