package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.DeleteProjectService
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.s3.S3BulkFileDeletionEvent
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DeleteProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : DeleteProjectService {
    override fun execute(projectId: Long) {
        transaction {
            val currentUser = currentMemberProvider.getCurrentMember()
            val project =
                projectExposedRepository.findProjectById(projectId)
                    ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)
            if (project.ownerId != currentUser.id) {
                throw GsmcException(ErrorCode.PROJECT_FORBIDDEN)
            }
            val scoreIds = projectExposedRepository.findScoreIdsByProjectId(projectId)
            val scores = scoreExposedRepository.findAllByIdIn(scoreIds)
            val sourceIds = scores.mapNotNull { it.sourceId }
            val evidences = evidenceExposedRepository.findAllByIdIn(sourceIds)
            val allFiles = (evidences.flatMap { it.files } + project.files).distinctBy { it.id }
            val evidenceIds = evidences.map { it.id }
            evidenceExposedRepository.deleteAllByIdIn(evidenceIds)
            scoreExposedRepository.deleteAllByIdIn(scoreIds)
            projectExposedRepository.deleteProjectById(projectId)
            val fileIds = allFiles.map { it.id }
            fileExposedRepository.deleteAllByIdIn(fileIds)
            val fileUris = allFiles.map { it.uri }
            eventPublisher.publishEvent(S3BulkFileDeletionEvent(fileUris))
        }
    }
}
