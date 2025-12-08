package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.BaseCreateOrUpdateBasedScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateJlptScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.event.alert.CreateAlertEvent
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
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
    private val eventPublisher: ApplicationEventPublisher,
    private val memberExposedRepository: MemberExposedRepository,
) : BaseCreateOrUpdateBasedScoreService(scoreExposedRepository, currentMemberProvider),
    CreateJlptScoreService {
    override fun execute(
        value: String,
        fileId: Long,
    ): CreateScoreResponse =
        transaction {
            if (!fileExposedRepository.existsById(fileId)) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }

            val intValue =
                value.toIntOrNull()
                    ?: throw GsmcException(ErrorCode.SCORE_INVALID_VALUE)

            if (intValue !in 1..5) {
                throw GsmcException(ErrorCode.SCORE_VALUE_OUT_OF_RANGE)
            }

            val member = currentMemberProvider.getCurrentMember()

            val score =
                createOrUpdateScore(
                    member = member,
                    categoryType = CategoryType.JLPT,
                    scoreValue = intValue.toDouble(),
                    sourceId = fileId,
                )
            val grade = member.grade
            val classNumber = member.classNumber

            if (grade != null && classNumber != null) {
                val homeroomTeacher =
                    memberExposedRepository
                        .findByGradeAndClassNumberAndRole(
                            grade = grade,
                            classNumber = classNumber,
                            role = MemberRole.HOMEROOM_TEACHER,
                        ).firstOrNull()

                homeroomTeacher?.let { teacher ->
                    eventPublisher.publishEvent(
                        CreateAlertEvent(
                            senderId = member.id,
                            receiverId = teacher.id,
                            scoreId = score.scoreId,
                            alertType = AlertType.ADD_SCORE,
                        ),
                    )
                }
            }
            score
        }
}
