package com.team.incube.gsmc.v3.domain.archive.service

interface ArchiveScoresByYearService {
    fun execute(academicYear: Int): Long
}
