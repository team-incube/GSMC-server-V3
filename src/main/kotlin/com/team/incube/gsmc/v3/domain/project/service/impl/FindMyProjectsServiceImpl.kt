package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.FindMyProjectsService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyProjectsServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindMyProjectsService {
    override fun execute(): List<GetProjectResponse> =
        transaction {
            val currentUser = currentMemberProvider.getCurrentMember()
            val projects = projectExposedRepository.findProjectsByParticipantId(currentUser.id)

            projects.map { project ->
                val scoreIds = projectExposedRepository.findScoreIdsByProjectId(project.id!!)
                val fileItems =
                    project.files.map { file ->
                        GetFileResponse(
                            id = file.id,
                            memberId = file.member,
                            originalName = file.originalName,
                            storeName = file.storeName,
                            uri = file.uri,
                        )
                    }
                GetProjectResponse(
                    id = project.id!!,
                    ownerId = project.ownerId,
                    title = project.title,
                    description = project.description,
                    files = fileItems,
                    participants = project.participants,
                    scoreIds = scoreIds,
                )
            }
        }
}
