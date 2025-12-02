package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.project.presentation.data.dto.ProjectParticipationScoreInfo
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetMyProjectScoreAndEvidenceResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.project.service.FindMyProjectScoreAndEvidenceService
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyProjectScoreAndEvidenceServiceImpl(
    private val projectExposedRepository: ProjectExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindMyProjectScoreAndEvidenceService {
    override fun execute(projectId: Long): GetMyProjectScoreAndEvidenceResponse =
        transaction {
            val currentMember = currentMemberProvider.getCurrentMember()

            val projectTitle =
                projectExposedRepository.findProjectTitleById(projectId)
                    ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)

            if (!projectExposedRepository.existsProjectParticipantByProjectIdAndMemberId(
                    projectId = projectId,
                    memberId = currentMember.id,
                )
            ) {
                throw GsmcException(ErrorCode.NOT_PROJECT_PARTICIPANT)
            }

            val targetScore =
                scoreExposedRepository.findProjectParticipationScore(
                    memberId = currentMember.id,
                    projectId = projectId,
                )

            if (targetScore == null) {
                return@transaction GetMyProjectScoreAndEvidenceResponse(
                    score = null,
                    evidence = null,
                )
            }

            val scoreResponse =
                ProjectParticipationScoreInfo(
                    scoreId = targetScore.id!!,
                    categoryNames =
                        CategoryNames(
                            koreanName = targetScore.categoryType.koreanName,
                            englishName = targetScore.categoryType.englishName,
                        ),
                    scoreStatus = targetScore.status,
                    activityName = targetScore.activityName,
                    scoreValue = targetScore.scoreValue,
                    rejectionReason = targetScore.rejectionReason,
                )

            val evidenceResponse =
                targetScore.sourceId
                    ?.takeIf { targetScore.categoryType.evidenceType == EvidenceType.EVIDENCE }
                    ?.let { evidenceId ->
                        evidenceExposedRepository.findById(evidenceId)?.let { evidence ->
                            GetEvidenceResponse(
                                evidenceId = evidence.id,
                                title = evidence.title,
                                content = evidence.content,
                                createdAt = evidence.createdAt,
                                updatedAt = evidence.updatedAt,
                                files =
                                    evidence.files.map { file ->
                                        GetFileResponse(
                                            id = file.id,
                                            originalName = file.originalName,
                                            storeName = file.storeName,
                                            uri = file.uri,
                                            memberId = file.member,
                                        )
                                    },
                            )
                        }
                    }

            GetMyProjectScoreAndEvidenceResponse(
                score = scoreResponse,
                evidence = evidenceResponse,
            )
        }
}
