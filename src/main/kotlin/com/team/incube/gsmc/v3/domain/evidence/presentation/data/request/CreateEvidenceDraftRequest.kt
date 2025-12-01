package com.team.incube.gsmc.v3.domain.evidence.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "증빙자료 임시저장 생성 요청")
data class CreateEvidenceDraftRequest(
    @param:Schema(description = "증빙자료 제목", example = "대회 참가 증빙")
    val title: String = "",
    @param:Schema(description = "증빙자료 내용", example = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.")
    val content: String = "",
    @param:Schema(description = "파일 ID 목록", example = "[1, 2]")
    val fileIds: List<Long> = emptyList(),
)
