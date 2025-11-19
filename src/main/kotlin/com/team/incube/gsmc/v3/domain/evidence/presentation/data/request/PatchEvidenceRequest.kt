package com.team.incube.gsmc.v3.domain.evidence.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "증빙자료 수정 요청")
data class PatchEvidenceRequest(
    @field:Schema(description = "참가자 ID", example = "1", nullable = true)
    val participantId: Long? = null,
    @field:Schema(description = "증빙자료 제목", example = "대회 참가 증빙", nullable = true)
    val title: String? = null,
    @field:Schema(description = "증빙자료 내용", example = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.", nullable = true)
    val content: String? = null,
    @field:Schema(description = "파일 ID 목록", example = "[1, 2]", nullable = true)
    val fileIds: List<Long>? = null,
)
