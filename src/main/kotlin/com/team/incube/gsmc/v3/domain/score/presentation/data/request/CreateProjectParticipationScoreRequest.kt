package com.team.incube.gsmc.v3.domain.score.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class CreateProjectParticipationScoreRequest(
    @param:Schema(description = "프로젝트 ID", example = "1")
    @field:NotNull
    val projectId: Long,
)
