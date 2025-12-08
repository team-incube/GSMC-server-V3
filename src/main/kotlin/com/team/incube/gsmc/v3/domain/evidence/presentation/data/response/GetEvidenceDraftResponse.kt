package com.team.incube.gsmc.v3.domain.evidence.presentation.data.response

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "증빙자료 임시저장 조회 응답")
data class GetEvidenceDraftResponse(
    @param:Schema(description = "증빙자료 제목", example = "대회 참가 증빙")
    val title: String,
    @param:Schema(description = "증빙자료 내용", example = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.")
    val content: String,
    @param:Schema(description = "파일 목록")
    val files: List<GetFileResponse>,
)
