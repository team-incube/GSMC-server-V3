package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.score.calculator.TotalScoreCalculator
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetTotalScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CalculateTotalScoreService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CalculateTotalScoreServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
    private val totalScoreCalculator: TotalScoreCalculator,
) : CalculateTotalScoreService {
    override fun execute(includeApprovedOnly: Boolean): GetTotalScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()
            val allScores = scoreExposedRepository.findAllByMemberId(member.id)
            val totalScore = totalScoreCalculator.calculate(allScores, includeApprovedOnly)
            GetTotalScoreResponse(totalScore = totalScore)
        }
}
