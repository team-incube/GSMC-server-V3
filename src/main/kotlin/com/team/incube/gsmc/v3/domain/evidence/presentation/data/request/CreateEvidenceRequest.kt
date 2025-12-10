package com.team.incube.gsmc.v3.domain.evidence.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "증빙자료 생성 요청")
data class CreateEvidenceRequest(
    @param:Schema(description = "점수 ID", example = "1")
    @field:NotNull(message = "점수 ID는 필수입니다")
    val scoreId: Long,
    @param:Schema(description = "증빙자료 제목", example = "대회 참가 증빙")
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(min = 1, max = 100)
    val title: String,
    @param:Schema(description = "증빙자료 내용", example = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.")
    @field:NotBlank(message = "내용은 필수입니다")
    @field:Size(min = 300, max = 2000)
    val content: String,
    @param:Schema(description = "파일 ID 목록", example = "[1, 2]")
    val fileIds: List<Long> = emptyList(),
)
