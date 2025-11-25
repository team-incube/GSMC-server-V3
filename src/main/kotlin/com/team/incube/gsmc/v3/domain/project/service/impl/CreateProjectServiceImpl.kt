package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.ProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.CreateProjectService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : CreateProjectService {
    override fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): ProjectResponse =
        transaction {
            val currentUser = currentMemberProvider.getCurrentMember()

            val project =
                projectExposedRepository.saveProject(
                    ownerId = currentUser.id,
                    title = title,
                    description = description,
                    fileIds = fileIds,
                    participantIds = participantIds,
                )

            ProjectResponse(
                id = project.id!!,
                ownerId = project.ownerId,
                title = project.title,
                description = project.description,
                files = project.files,
                participants = project.participants,
            )
        }
}
