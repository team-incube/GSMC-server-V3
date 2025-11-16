package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.dto.Project
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
    override fun execute(): List<Project> =
        transaction {
            val currentUser = currentMemberProvider.getCurrentUser()
            projectExposedRepository.findProjectsByParticipantId(currentUser.id)
        }
}