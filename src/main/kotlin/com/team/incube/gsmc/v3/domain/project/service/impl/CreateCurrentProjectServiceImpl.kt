package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.CreateCurrentProjectService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateCurrentProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : CreateCurrentProjectService {
    override fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project =
        transaction {
            val currentUser = currentMemberProvider.getCurrentUser()

            projectExposedRepository.saveProject(
                ownerId = currentUser.id,
                title = title,
                description = description,
                fileIds = fileIds,
                participantIds = participantIds,
            )
        }
}
