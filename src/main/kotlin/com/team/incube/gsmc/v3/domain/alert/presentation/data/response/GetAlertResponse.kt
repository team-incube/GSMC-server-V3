package com.team.incube.gsmc.v3.domain.alert.presentation.data.response

import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class GetAlertResponse(
    @field:Schema(description = "알림 ID", example = "1")
    val id: Long,
    @field:Schema(description = "알림 제목", example = "합격 알림")
    val title: String,
    @field:Schema(description = "알림 내용", example = "정보처리산업기사 자격증을 담임선생님께서 통과시키셨습니다.")
    val content: String,
    @field:Schema(description = "알림 생성 시간", example = "2025-10-30")
    val createdAt: LocalDate,
    @field:Schema(description = "알림 타입", example = "APPROVED")
    val alertType: AlertType,
    @field:Schema(description = "점수 ID", example = "1")
    val scoreId: Long,
)
