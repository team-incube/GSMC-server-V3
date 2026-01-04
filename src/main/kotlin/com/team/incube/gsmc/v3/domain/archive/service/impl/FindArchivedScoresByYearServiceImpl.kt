package com.team.incube.gsmc.v3.domain.archive.service.impl

import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchive
import com.team.incube.gsmc.v3.domain.archive.mapper.ScoreArchiveMapper
import com.team.incube.gsmc.v3.domain.archive.service.FindArchivedScoresByYearService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FindArchivedScoresByYearServiceImpl(
    private val scoreArchiveMapper: ScoreArchiveMapper,
) : FindArchivedScoresByYearService {
    @Transactional(readOnly = true)
    override fun execute(academicYear: Int): List<ScoreArchive> {
        val archives = scoreArchiveMapper.findByAcademicYear(academicYear)

        if (archives.isEmpty()) {
            throw GsmcException(ErrorCode.ARCHIVE_NOT_FOUND)
        }

        return archives
    }
}
