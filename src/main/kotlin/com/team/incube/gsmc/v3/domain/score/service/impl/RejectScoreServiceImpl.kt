package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.RejectScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class RejectScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
) : RejectScoreService {
    override fun execute(
        scoreId: Long,
        rejectionReason: String,
    ) {
        transaction {
            if (!scoreExposedRepository.existsById(scoreId)) {
                throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
            }

            scoreExposedRepository.updateStatusAndRejectionReasonByScoreId(
                scoreId = scoreId,
                status = ScoreStatus.REJECTED,
                rejectionReason = rejectionReason,
            )
        }
    }
}
