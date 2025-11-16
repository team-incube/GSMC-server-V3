package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.SearchProjectService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class SearchProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
) : SearchProjectService {
    override fun execute(
        title: String?,
        pageable: Pageable,
    ): Page<Project> =
        transaction {
            projectExposedRepository.searchProjects(
                title = title,
                pageable = pageable,
            )
        }
}