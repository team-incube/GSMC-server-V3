package com.team.incube.gsmc.v3.domain.sheet.dto

data class ClassScoreData(
    val studentId: Long,
    val studentName: String,
    val studentNumber: String,
    val categoryScores: Map<String, Double>,
    val totalScore: Double,
    val classRank: Int,
)
