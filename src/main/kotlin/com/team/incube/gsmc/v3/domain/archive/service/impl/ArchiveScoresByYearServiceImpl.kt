package com.team.incube.gsmc.v3.domain.archive.service.impl

import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchive
import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchiveSnapshot
import com.team.incube.gsmc.v3.domain.archive.mapper.ScoreArchiveMapper
import com.team.incube.gsmc.v3.domain.archive.service.ArchiveScoresByYearService
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Service
class ArchiveScoresByYearServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val scoreArchiveMapper: ScoreArchiveMapper,
    private val objectMapper: ObjectMapper,
) : ArchiveScoresByYearService {
    @Transactional
    override fun execute(academicYear: Int): Long {
        scoreArchiveMapper.deleteByAcademicYear(academicYear)
        val accumulatedCategories =
            CategoryType
                .getAllCategories()
                .filter { it.isAccumulated }
        val allApprovedScores =
            transaction {
                scoreExposedRepository.findAllByStatusAndCategoryTypeIn(
                    ScoreStatus.APPROVED,
                    accumulatedCategories,
                )
            }
        if (allApprovedScores.isEmpty()) {
            throw GsmcException(ErrorCode.NO_SCORES_TO_ARCHIVE)
        }
        val archives =
            allApprovedScores.map { score ->
                createArchive(academicYear, score)
            }
        val batchSize = 1000
        var totalInserted = 0L
        archives.chunked(batchSize).forEach { batch ->
            val inserted = scoreArchiveMapper.insertBatch(batch)
            totalInserted += inserted
        }
        return totalInserted
    }

    private fun createArchive(
        academicYear: Int,
        score: Score,
    ): ScoreArchive {
        val snapshot =
            ScoreArchiveSnapshot(
                scoreId = score.id ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND),
                status = score.status,
                sourceId = score.sourceId,
                activityName = score.activityName,
                scoreValue = score.scoreValue,
                rejectionReason = score.rejectionReason,
                updatedAt = score.updatedAt,
            )
        val snapshotJson = objectMapper.writeValueAsString(snapshot)

        return ScoreArchive(
            archiveId = null,
            academicYear = academicYear,
            archivedAt = null,
            memberEmail = score.member.email,
            memberName = score.member.name,
            memberGrade = score.member.grade,
            memberClassNumber = score.member.classNumber,
            memberNumber = score.member.number,
            categoryEnglishName = score.categoryType.englishName,
            categoryKoreanName = score.categoryType.koreanName,
            scoreSnapshot = snapshotJson,
        )
    }
}
