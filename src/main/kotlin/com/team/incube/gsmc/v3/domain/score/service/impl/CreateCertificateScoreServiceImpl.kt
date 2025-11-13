package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.category.dto.Category
import com.team.incube.gsmc.v3.domain.category.dto.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CreateCertificateScoreService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateCertificateScoreServiceImpl(
    private final val scoreExposedRepository: ScoreExposedRepository,
    private final val fileExposedRepository: FileExposedRepository,
    private final val currentMemberProvider: CurrentMemberProvider
) : CreateCertificateScoreService {
    override fun execute(certificateName: String, fileId: Long): CreateScoreResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()
            if (fileExposedRepository.existsById(fileId).not()) {
                throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            }
            val savedScore = scoreExposedRepository.save(
                Score(
                    id = null,
                    member = member,
                    category = mockCategory(),
                    status = ScoreStatus.PENDING,
                    sourceId = fileId,
                    activityName = certificateName,
                )
            )
            CreateScoreResponse(
                scoreId = savedScore.id!!,
                categoryNames = CategoryNames(
                    koreanName = savedScore.category.koreanName,
                    englishName = savedScore.category.englishName
                ),
                scoreStatus = savedScore.status,
                sourceId = savedScore.sourceId,
                activityName = savedScore.activityName
            )
        }

    private fun mockCategory(): Category {
        return Category(
            id = 1L,
            englishName = "",
            koreanName = "",
            weight = 1,
            maximumValue = 1,
            isAccumulated = false,
            evidenceType = EvidenceType.FILE,
        )
    }
}
