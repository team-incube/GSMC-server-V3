package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.presentation.data.request.CreateProjectDraftRequest
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectDraftResponse
import com.team.incube.gsmc.v3.domain.project.service.CreateProjectDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service

@Service
class CreateProjectDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
) : CreateProjectDraftService {
    @CachePut(
        value = ["projectDraft"],
        key = "#root.target.getMemberId()",
        unless = "#result == null",
    )
    override fun execute(request: CreateProjectDraftRequest): GetProjectDraftResponse =
        GetProjectDraftResponse(
            title = request.title,
            description = request.description,
            fileIds = request.fileIds,
            participantIds = request.participantIds,
        )

    fun getMemberId(): Long = currentMemberProvider.getCurrentMemberId()
}
