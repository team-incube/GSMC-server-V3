package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.UpdateScoreStatusService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UpdateScoreStatusServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val memberExposedRepository: MemberExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : UpdateScoreStatusService {
    override fun execute(
        scoreId: Long,
        scoreStatus: ScoreStatus,
    ) = transaction {
        val score =
            scoreExposedRepository.findById(scoreId)
                ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        val currentMember = currentMemberProvider.getCurrentMember()
        if (currentMember.role == MemberRole.HOMEROOM_TEACHER) {
            val student =
                memberExposedRepository.findById(score.member.id)
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            if (student.grade != currentMember.grade || student.classNumber != currentMember.classNumber) {
                throw GsmcException(ErrorCode.SCORE_NOT_OWNED)
            }
        }
        val updatedCount = scoreExposedRepository.updateStatusByScoreId(scoreId, scoreStatus)
        if (updatedCount == 0) {
            throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        }
    }
}
