package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetStudentPercentResponse
import com.team.incube.gsmc.v3.domain.score.service.CalculateTotalScoreByMemberIdService
import com.team.incube.gsmc.v3.domain.score.service.FindMyPercentInGradeService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyPercentInGradeServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val memberExposedRepository: MemberExposedRepository,
    private val calculateTotalScoreByMemberIdService: CalculateTotalScoreByMemberIdService,
) : FindMyPercentInGradeService {
    override fun execute(): GetStudentPercentResponse =
        transaction {
            val currentMember = currentMemberProvider.getCurrentMember()

            val grade = currentMember.grade ?: throw GsmcException(ErrorCode.MEMBER_GRADE_CLASS_NOT_SET)

            val studentsInGrade = memberExposedRepository.findStudentsByGrade(grade)

            if (studentsInGrade.size == 1) {
                return@transaction GetStudentPercentResponse(
                    topPercentile = 100.0,
                    bottomPercentile = 0.0,
                )
            }

            val totalScoresByMember =
                studentsInGrade.map { student ->
                    val totalScore = calculateTotalScoreByMemberIdService.execute(student.id, includeApprovedOnly = true).totalScore
                    student to totalScore
                }

            val myTotalScore = totalScoresByMember.find { it.first.id == currentMember.id }?.second ?: 0

            val lowerScoreCount = totalScoresByMember.count { it.second < myTotalScore }
            val totalCount = totalScoresByMember.size

            val topPercentile = (lowerScoreCount.toDouble() / totalCount) * 100
            val bottomPercentile = 100.0 - topPercentile

            GetStudentPercentResponse(
                topPercentile = topPercentile,
                bottomPercentile = bottomPercentile,
            )
        }
}
