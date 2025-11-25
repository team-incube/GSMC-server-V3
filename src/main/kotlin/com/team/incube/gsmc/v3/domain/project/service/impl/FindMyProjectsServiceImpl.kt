package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.ProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.FindCurrentProjectsService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindCurrentProjectsServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindCurrentProjectsService {
    override fun execute(): List<ProjectResponse> =
        transaction {
            val currentUser = currentMemberProvider.getCurrentMember()
            val projects = projectExposedRepository.findProjectsByParticipantId(currentUser.id)

            projects.map { project ->
                val scoreItems = projectExposedRepository.getProjectScoreItems(project.id!!)
                ProjectResponse(
                    id = project.id!!,
                    ownerId = project.ownerId,
                    title = project.title,
                    description = project.description,
                    files = project.files,
                    participants = project.participants,
                    scoreItems = scoreItems,
                )
            }
        }
}
