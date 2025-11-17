package com.team.incube.gsmc.v3.domain.evidence.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "내 증빙자료 목록 조회 응답")
data class GetMyEvidencesResponse(
    @field:Schema(description = "증빙자료 목록")
    val evidences: List<GetEvidenceResponse>,
)
