package com.team.incube.gsmc.v3.domain.evidence.presentation.data.response

import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "증빙자료 수정 응답")
data class PatchEvidenceResponse(
    @param:Schema(description = "증빙자료 ID", example = "1")
    val id: Long,
    @param:Schema(description = "증빙자료 제목", example = "2025년 전국 SW 마이스터고 연합 해커톤")
    val title: String,
    @param:Schema(description = "증빙자료 내용", example = "2025년 전국 SW 마이스터고 연합 해커톤 참가했습니다!.")
    val content: String,
    @param:Schema(description = "생성 일시", example = "2024-10-02T10:30:00")
    val createAt: LocalDateTime,
    @param:Schema(description = "수정 일시", example = "2024-10-02T10:30:00")
    val updateAt: LocalDateTime,
    @param:Schema(description = "파일 목록")
    val files: List<FileItem>,
)
