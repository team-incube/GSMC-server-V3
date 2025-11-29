package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
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

            // 1. 모든 관련 점수 조회
            val scores =
                scoreExposedRepository.findAllByActivityNameAndCategoryType(
                    activityName = project.title,
                    categoryType = CategoryType.PROJECT_PARTICIPATION,
                )

            // 2. 모든 sourceId 수집 및 벌크로 evidence 조회
            val sourceIds = scores.mapNotNull { it.sourceId }
            val evidences =
                if (sourceIds.isNotEmpty()) {
                    evidenceExposedRepository.findAllByIdIn(sourceIds)
                } else {
                    emptyList()
                }

            // 3. 모든 파일 수집
            val allFiles = evidences.flatMap { it.files }

            // 4. S3에서 파일 삭제 (순차 처리)
            allFiles.forEach { file ->
                s3DeleteService.execute(file.uri)
            }

            // 5. 벌크 삭제
            val fileIds = allFiles.map { it.id }
            if (fileIds.isNotEmpty()) {
                fileExposedRepository.deleteAllByIdIn(fileIds)
            }

            val evidenceIds = evidences.mapNotNull { it.id }
            if (evidenceIds.isNotEmpty()) {
                evidenceExposedRepository.deleteAllByIdIn(evidenceIds)
            }

            val scoreIds = scores.mapNotNull { it.id }
            if (scoreIds.isNotEmpty()) {
                scoreExposedRepository.deleteAllByIdIn(scoreIds)
            }

            // 6. 프로젝트 삭제
            projectExposedRepository.deleteProjectById(projectId)
        }
    }
}
