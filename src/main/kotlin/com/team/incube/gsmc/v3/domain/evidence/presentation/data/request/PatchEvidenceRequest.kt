package com.team.incube.gsmc.v3.domain.evidence.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "증빙자료 수정 요청")
data class PatchEvidenceRequest(
    @param:Schema(description = "점수 ID", example = "1", nullable = true)
    val scoreId: Long? = null,
    @param:Schema(description = "증빙자료 제목", example = "대회 참가 증빙", nullable = true)
    @field:Size(min = 1, max = 100)
    val title: String? = null,
    @param:Schema(description = "증빙자료 내용", example = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.", nullable = true)
    @field:Size(min = 300, max = 2000)
    val content: String? = null,
    @param:Schema(description = "파일 ID 목록", example = "[1, 2]", nullable = true)
    val fileIds: List<Long>? = null,
)
