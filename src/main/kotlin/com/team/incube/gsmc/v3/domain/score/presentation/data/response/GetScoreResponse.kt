package com.team.incube.gsmc.v3.domain.score.presentation.data.response

import com.team.incube.gsmc.v3.domain.evidence.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.CategoryNames
import com.team.incube.gsmc.v3.domain.score.presentation.data.dto.EvidenceDetail
import io.swagger.v3.oas.annotations.media.Schema

data class GetScoreResponse(
    @param:Schema(description = "점수 ID", example = "1")
    val scoreId: Long,
    val categoryNames: CategoryNames,
    @param:Schema(description = "인증제 점수 상태", example = "PENDING")
    val scoreStatus: ScoreStatus,
    @param:Schema(description = "참여 활동명", example = "정보처리기능사")
    val activityName: String?,
    @param:Schema(description = "점수 값", example = "95.5")
    val scoreValue: Double?,
    @param:Schema(description = "증거자료 상세 정보")
    val evidence: EvidenceDetail?,
)