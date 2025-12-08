package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.UpdateScoreStatusService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UpdateScoreStatusServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
) : UpdateScoreStatusService {
    override fun execute(
        scoreId: Long,
        scoreStatus: ScoreStatus,
    ) = transaction {
        val updatedCount = scoreExposedRepository.updateStatusByScoreId(scoreId, scoreStatus)
        if (updatedCount == 0) {
            throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        }
    }
}
