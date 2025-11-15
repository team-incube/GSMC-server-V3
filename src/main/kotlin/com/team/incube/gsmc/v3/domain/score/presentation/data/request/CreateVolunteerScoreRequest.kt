package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateVolunteerScoreRequest(
    @param:Schema(description = "봉사 시간 (1시간당 1점, 최대 10점)", example = "5")
    @field:Min(value = 1)
    @field:NotNull
    val hours: Int,
)
