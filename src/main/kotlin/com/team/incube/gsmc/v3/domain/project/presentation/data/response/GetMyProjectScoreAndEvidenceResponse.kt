package com.team.incube.gsmc.v3.domain.project.presentation.data.response

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoreResponse
import io.swagger.v3.oas.annotations.media.Schema

data class GetMyProjectScoreAndEvidenceResponse(
    @param:Schema(description = "프로젝트 참여 점수 정보 (작성하지 않았으면 null)", nullable = true)
    val score: GetScoreResponse?,
    @param:Schema(description = "프로젝트 참여 증빙자료 정보 (작성하지 않았으면 null)", nullable = true)
    val evidence: GetEvidenceResponse?,
)
