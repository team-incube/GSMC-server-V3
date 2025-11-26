package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.service.DeleteEvidenceDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service

@Service
class DeleteEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
) : DeleteEvidenceDraftService {
    @CacheEvict(value = ["evidenceDraft"], key = "#root.target.getMemberId()")
    override fun execute() {}

    fun getMemberId(): Long = currentMemberProvider.getCurrentMemberId()
}
