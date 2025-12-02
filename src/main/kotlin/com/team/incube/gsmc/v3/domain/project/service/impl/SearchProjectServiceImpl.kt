package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectResponse
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.SearchProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.SearchProjectService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SearchProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
) : SearchProjectService {
    override fun execute(
        title: String?,
        pageable: Pageable,
    ): SearchProjectResponse =
        transaction {
            val projectPage =
                projectExposedRepository.searchProjects(
                    title = title,
                    pageable = pageable,
                )

            val projects =
                projectPage.content.map { project ->
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

            SearchProjectResponse(
                projects = projects,
                totalPages = projectPage.totalPages,
                totalElements = projectPage.totalElements,
            )
        }
}
