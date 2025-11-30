package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.redis.EvidenceDraftRedisRepository
import com.team.incube.gsmc.v3.domain.evidence.service.DeleteEvidenceDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.stereotype.Service

@Service
class DeleteEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val evidenceDraftRedisRepository: EvidenceDraftRedisRepository,
) : DeleteEvidenceDraftService {
    override fun execute() {
        val memberId = currentMemberProvider.getCurrentMemberId()
        evidenceDraftRedisRepository.deleteById(memberId)
    }
}
