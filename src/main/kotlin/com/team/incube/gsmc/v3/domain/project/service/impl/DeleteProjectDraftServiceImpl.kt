package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.service.DeleteProjectDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service

@Service
class DeleteProjectDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
) : DeleteProjectDraftService {
    @CacheEvict(
        value = ["projectDraft"],
        key = "#root.target.getMemberId()",
    )
    override fun execute() {}

    fun getMemberId(): Long = currentMemberProvider.getCurrentMemberId()
}
