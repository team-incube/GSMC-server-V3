package com.team.incube.gsmc.v3.domain.archive.service

import com.team.incube.gsmc.v3.domain.archive.dto.ScoreArchive

interface FindArchivedScoresByYearService {
    fun execute(academicYear: Int): List<ScoreArchive>
}
