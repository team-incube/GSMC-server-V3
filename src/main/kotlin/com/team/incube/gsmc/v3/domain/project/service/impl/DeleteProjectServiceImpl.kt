package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.DeleteProjectService
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteProjectServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val s3DeleteService: S3DeleteService,
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

            // 1. 프로젝트에 연결된 점수 ID 조회
            val scoreIds = projectExposedRepository.findScoreIdsByProjectId(projectId)

            // 2. 모든 점수를 벌크로 조회하고 sourceId 수집 후 evidence 조회
            val scores = scoreExposedRepository.findAllByIdIn(scoreIds)
            val sourceIds = scores.mapNotNull { it.sourceId }
            val evidences = evidenceExposedRepository.findAllByIdIn(sourceIds)

            // 3. 모든 파일 수집 (evidence 파일 + 프로젝트 직접 첨부 파일)
            val allFiles = (evidences.flatMap { it.files } + project.files).distinctBy { it.id }

            // 4. S3에서 파일 삭제 (순차 처리)
            allFiles.forEach { file ->
                s3DeleteService.execute(file.uri)
            }

            // 5. 벌크 삭제
            val fileIds = allFiles.map { it.id }
            fileExposedRepository.deleteAllByIdIn(fileIds)

            val evidenceIds = evidences.mapNotNull { it.id }
            evidenceExposedRepository.deleteAllByIdIn(evidenceIds)

            scoreExposedRepository.deleteAllByIdIn(scoreIds)

            // 6. 프로젝트 삭제
            projectExposedRepository.deleteProjectById(projectId)
        }
    }
}
