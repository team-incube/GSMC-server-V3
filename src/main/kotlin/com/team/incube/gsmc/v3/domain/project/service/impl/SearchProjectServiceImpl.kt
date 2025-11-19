package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.ProjectResponse
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
                    ProjectResponse(
                        id = project.id!!,
                        ownerId = project.ownerId,
                        title = project.title,
                        description = project.description,
                        files = project.files,
                        participants = project.participants,
                    )
                }

            SearchProjectResponse(
                projects = projects,
                totalPages = projectPage.totalPages,
                totalElements = projectPage.totalElements,
            )
        }
}
