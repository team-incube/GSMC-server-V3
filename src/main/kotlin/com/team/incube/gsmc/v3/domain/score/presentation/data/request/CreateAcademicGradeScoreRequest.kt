package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

data class CreateAcademicGradeScoreRequest(
    @param:Schema(description = "평균등급", example = "2.5")
    @field:NotNull
    @field:DecimalMin(value = "1.0")
    @field:DecimalMax(value = "9.0")
    val averageGrade: Double,
)