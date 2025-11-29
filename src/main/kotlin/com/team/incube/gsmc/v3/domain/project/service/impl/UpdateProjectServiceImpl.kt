package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.UpdateProjectService
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UpdateProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : UpdateProjectService {
    override fun execute(
        projectId: Long,
        title: String?,
        description: String?,
        fileIds: List<Long>?,
        participantIds: List<Long>?,
    ): GetProjectResponse =
        transaction {
            val currentUser = currentMemberProvider.getCurrentMember()
            val project =
                projectExposedRepository.findProjectById(projectId)
                    ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)

            if (project.ownerId != currentUser.id) {
                throw GsmcException(ErrorCode.PROJECT_FORBIDDEN)
            }

            val newTitle = title ?: project.title

            // 프로젝트 제목이 변경된 경우 관련된 점수의 activityName 동기화
            if (title != null && title != project.title) {
                val scoreIds = projectExposedRepository.findScoreIdsByProjectId(projectId)
                scoreIds.forEach { scoreId ->
                    scoreExposedRepository.updateActivityName(scoreId, newTitle)
                }
            }

            val updatedProject =
                projectExposedRepository.updateProject(
                    id = projectId,
                    ownerId = project.ownerId,
                    title = newTitle,
                    description = description ?: project.description,
                    fileIds = fileIds ?: project.files.map { it.id },
                    participantIds = participantIds ?: project.participants.map { it.id },
                )

            val scoreIds = projectExposedRepository.findScoreIdsByProjectId(projectId)
            val fileItems =
                updatedProject.files.map { file ->
                    FileItem(
                        id = file.id,
                        member = file.member,
                        originalName = file.originalName,
                        storeName = file.storeName,
                        uri = file.uri,
                    )
                }

            GetProjectResponse(
                id = updatedProject.id!!,
                ownerId = updatedProject.ownerId,
                title = updatedProject.title,
                description = updatedProject.description,
                files = fileItems,
                participants = updatedProject.participants,
                scoreIds = scoreIds,
            )
        }
}
