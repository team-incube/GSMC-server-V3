package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.DeleteCurrentProjectService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteCurrentProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : DeleteCurrentProjectService {
    override fun execute(projectId: Long) {
        transaction {
            val currentUser = currentMemberProvider.getCurrentUser()
            val project =
                projectExposedRepository.findProjectById(projectId)
                    ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)

            if (project.ownerId != currentUser.id) {
                throw GsmcException(ErrorCode.PROJECT_FORBIDDEN)
            }

            projectExposedRepository.deleteProjectById(projectId)
        }
    }
}
