package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceDraftResponse
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class FindEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
) : FindEvidenceDraftService {
    @Cacheable(value = ["evidenceDraft"], key = "#root.target.getMemberId()")
    override fun execute(): GetEvidenceDraftResponse? = null

    fun getMemberId(): Long = currentMemberProvider.getCurrentMember().id
}
