package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CreateReadAThonScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

/**
 * 빛고을독서마라톤 점수 생성/갱신 서비스
 *
 * 단계(1-7)는 Score.scoreValue 필드에 저장됩니다.
 * activityName은 사용하지 않습니다 (null).
 */
@Service
class CreateReadAThonScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : CreateReadAThonScoreService {
    override fun execute(
        grade: Int,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()

            if (fileExposedRepository.existsById(fileId).not()) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            val existingScore =
                scoreExposedRepository.findByMemberIdAndCategoryType(
                    memberId = member.id,
                    categoryType = CategoryType.READ_A_THON,
                )

            val savedScore =
                if (existingScore != null) {
                    scoreExposedRepository.update(
                        existingScore.copy(
                            status = ScoreStatus.PENDING,
                            sourceId = fileId,
                            scoreValue = grade,
                        ),
                    )
                } else {
                    scoreExposedRepository.save(
                        Score(
                            id = null,
                            member = member,
                            categoryType = CategoryType.READ_A_THON,
                            status = ScoreStatus.PENDING,
                            sourceId = fileId,
                            activityName = null,
                            scoreValue = grade,
                        ),
                    )
                }

            CreateScoreResponse(
                scoreId = savedScore.id!!,
                categoryNames =
                    CategoryNames(
                        koreanName = savedScore.categoryType.koreanName,
                        englishName = savedScore.categoryType.englishName,
                    ),
                scoreStatus = savedScore.status,
                sourceId = savedScore.sourceId,
                activityName = savedScore.activityName,
            )
        }
}
