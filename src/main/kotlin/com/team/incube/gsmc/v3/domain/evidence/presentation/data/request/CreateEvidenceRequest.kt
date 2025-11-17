package com.team.incube.gsmc.v3.domain.evidence.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "증빙자료 생성 요청")
data class CreateEvidenceRequest(
    @field:Schema(description = "점수 ID", example = "1")
    @field:NotNull(message = "점수 ID는 필수입니다")
    val scoreId: Long,
    @field:Schema(description = "증빙자료 제목", example = "대회 참가 증빙")
    @field:NotBlank(message = "제목은 필수입니다")
    val title: String,
    @field:Schema(description = "증빙자료 내용", example = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.")
    @field:NotBlank(message = "내용은 필수입니다")
    val content: String,
    @field:Schema(description = "파일 ID 목록", example = "[1, 2]")
    val fileId: List<Long> = emptyList(),
)
