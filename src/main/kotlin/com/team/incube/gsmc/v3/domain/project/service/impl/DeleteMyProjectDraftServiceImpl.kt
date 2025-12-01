package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.repository.ProjectDraftRedisRepository
import com.team.incube.gsmc.v3.domain.project.service.DeleteMyProjectDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.springframework.stereotype.Service

@Service
class DeleteMyProjectDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val projectDraftRedisRepository: ProjectDraftRedisRepository,
) : DeleteMyProjectDraftService {
    override fun execute() {
        val memberId = currentMemberProvider.getCurrentMemberId()
        projectDraftRedisRepository.deleteById(memberId)
    }
}
