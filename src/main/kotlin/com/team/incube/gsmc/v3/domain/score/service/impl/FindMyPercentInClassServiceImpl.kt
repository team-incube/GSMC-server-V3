package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.calculator.TotalScoreCalculator
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetStudentPercentResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.FindMyPercentInClassService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyPercentInClassServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val memberExposedRepository: MemberExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val totalScoreCalculator: TotalScoreCalculator,
) : FindMyPercentInClassService {
    override fun execute(includeApprovedOnly: Boolean): GetStudentPercentResponse =
        transaction {
            val currentMember = currentMemberProvider.getCurrentMember()

            val grade = currentMember.grade ?: throw GsmcException(ErrorCode.MEMBER_GRADE_CLASS_NOT_SET)
            val classNumber = currentMember.classNumber ?: throw GsmcException(ErrorCode.MEMBER_GRADE_CLASS_NOT_SET)

            val studentsInClass = memberExposedRepository.findStudentsByGradeAndClassNumber(grade, classNumber)

            if (studentsInClass.size <= 1) {
                return@transaction GetStudentPercentResponse(
                    topPercentile = 100.0,
                    bottomPercentile = 0.0,
                )
            }

            val memberIds = studentsInClass.map { it.id }
            val allScores = scoreExposedRepository.findAllByMemberIds(memberIds)
            val scoresByMemberId = allScores.groupBy { it.member.id }

            val totalScoresByMember =
                studentsInClass.map { student ->
                    val studentScores = scoresByMemberId[student.id] ?: emptyList()
                    val totalScore = totalScoreCalculator.calculate(studentScores, includeApprovedOnly)
                    student to totalScore
                }

            val myTotalScore = totalScoresByMember.find { it.first.id == currentMember.id }?.second ?: 0

            val lowerScoreCount = totalScoresByMember.count { it.second < myTotalScore }
            val totalCount = totalScoresByMember.size

            val percentileRank = (lowerScoreCount.toDouble() / totalCount) * 100
            val topPercentile = 100.0 - percentileRank
            val bottomPercentile = percentileRank

            GetStudentPercentResponse(
                topPercentile = topPercentile,
                bottomPercentile = bottomPercentile,
            )
        }
}
