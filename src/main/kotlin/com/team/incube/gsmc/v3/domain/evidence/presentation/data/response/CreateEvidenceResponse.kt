package com.team.incube.gsmc.v3.domain.evidence.presentation.data.response

import com.team.incube.gsmc.v3.domain.file.dto.File
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "증빙자료 생성 응답")
data class CreateEvidenceResponse(
    @field:Schema(description = "증빙자료 ID", example = "1")
    val id: Long,
    @field:Schema(description = "증빙자료 제목", example = "대회 참가 증빙")
    val title: String,
    @field:Schema(description = "증빙자료 내용", example = "2024년 전국 프로그래밍 대회 참가 증빙자료입니다.")
    val content: String,
    @field:Schema(description = "생성 일시", example = "2024-10-02T10:30:00")
    val createAt: LocalDateTime,
    @field:Schema(description = "수정 일시", example = "2024-10-02T10:30:00")
    val updateAt: LocalDateTime,
    @field:Schema(description = "파일 목록")
    val file: List<File>,
)
