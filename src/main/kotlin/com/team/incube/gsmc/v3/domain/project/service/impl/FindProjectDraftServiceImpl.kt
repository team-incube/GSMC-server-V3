package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectDraftResponse
import com.team.incube.gsmc.v3.domain.project.service.FindProjectDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class FindProjectDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
) : FindProjectDraftService {
    @Cacheable(value = ["projectDraft"], key = "#root.target.getMemberId()")
    override fun execute(): GetProjectDraftResponse? = null

    fun getMemberId(): Long = currentMemberProvider.getCurrentMemberId()
}
