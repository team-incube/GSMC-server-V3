package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.FindProjectByIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindProjectByIdServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
) : FindProjectByIdService {
    override fun execute(projectId: Long): Project =
        transaction {
            projectExposedRepository.findProjectById(projectId)
                ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)
        }
}