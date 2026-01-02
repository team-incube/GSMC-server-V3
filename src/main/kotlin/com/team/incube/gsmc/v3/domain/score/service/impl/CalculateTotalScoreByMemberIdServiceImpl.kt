package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.calculator.TotalScoreCalculator
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetTotalScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CalculateTotalScoreByMemberIdService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CalculateTotalScoreByMemberIdServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val memberExposedRepository: MemberExposedRepository,
    private val totalScoreCalculator: TotalScoreCalculator,
) : CalculateTotalScoreByMemberIdService {
    override fun execute(
        memberId: Long,
        includeApprovedOnly: Boolean,
    ): GetTotalScoreResponse =
        transaction {
            val member =
                memberExposedRepository.findById(memberId)
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)

            val allScores = scoreExposedRepository.findAllByMemberId(member.id)
            val totalScore = totalScoreCalculator.calculate(allScores, includeApprovedOnly)
            GetTotalScoreResponse(totalScore = totalScore)
        }
}
