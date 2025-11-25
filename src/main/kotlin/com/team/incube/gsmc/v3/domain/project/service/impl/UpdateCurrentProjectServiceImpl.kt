package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.ProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.UpdateCurrentProjectService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UpdateCurrentProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : UpdateCurrentProjectService {
    override fun execute(
        projectId: Long,
        title: String?,
        description: String?,
        fileIds: List<Long>?,
        participantIds: List<Long>?,
    ): ProjectResponse =
        transaction {
            val currentUser = currentMemberProvider.getCurrentMember()
            val project =
                projectExposedRepository.findProjectById(projectId)
                    ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)

            if (project.ownerId != currentUser.id) {
                throw GsmcException(ErrorCode.PROJECT_FORBIDDEN)
            }

            val updatedProject =
                projectExposedRepository.updateProject(
                    id = projectId,
                    ownerId = project.ownerId,
                    title = title ?: project.title,
                    description = description ?: project.description,
                    fileIds = fileIds ?: project.files.map { it.id },
                    participantIds = participantIds ?: project.participants.map { it.id },
                )

            ProjectResponse(
                id = updatedProject.id!!,
                ownerId = updatedProject.ownerId,
                title = updatedProject.title,
                description = updatedProject.description,
                files = updatedProject.files,
                participants = updatedProject.participants,
            )
        }
}
