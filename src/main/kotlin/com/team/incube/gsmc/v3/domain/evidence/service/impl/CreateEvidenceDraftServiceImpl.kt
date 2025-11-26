package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.CreateEvidenceDraftRequest
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceDraftResponse
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service

@Service
class CreateEvidenceDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
) : CreateEvidenceDraftService {
    @CachePut(value = ["evidenceDraft"], key = "#root.target.getMemberId()")
    override fun execute(request: CreateEvidenceDraftRequest): GetEvidenceDraftResponse =
        GetEvidenceDraftResponse(
            scoreId = request.scoreId,
            title = request.title,
            content = request.content,
            fileIds = request.fileIds,
        )

    fun getMemberId(): Long = currentMemberProvider.getCurrentMemberId()
}
