package com.team.incube.gsmc.v3.domain.archive.dto

import java.time.Instant

data class ScoreArchive(
    val archiveId: Long?,
    val academicYear: Int,
    val archivedAt: Instant?,
    val memberEmail: String,
    val memberName: String,
    val memberGrade: Int?,
    val memberClassNumber: Int?,
    val memberNumber: Int?,
    val categoryEnglishName: String,
    val categoryKoreanName: String,
    val scoreSnapshot: String,
)
