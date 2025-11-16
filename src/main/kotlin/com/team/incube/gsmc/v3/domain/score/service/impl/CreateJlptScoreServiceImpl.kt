package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCreateOrUpdateBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateJlptScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

/**
 * JLPT 점수 생성/갱신 서비스
 *
 * JLPT 등급(1-5)은 Score.scoreValue 필드에 저장됩니다.
 * activityName은 사용하지 않습니다 (null).
 */
@Service
class CreateJlptScoreServiceImpl(
    scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    currentMemberProvider: CurrentMemberProvider,
) : BaseCreateOrUpdateBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateJlptScoreService {
    override fun execute(
        grade: Int,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            if (!fileExposedRepository.existsById(fileId)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            createOrUpdateScore(
                categoryType = CategoryType.JLPT,
                scoreValue = grade.toDouble(),
                sourceId = fileId,
            )
        }
}
