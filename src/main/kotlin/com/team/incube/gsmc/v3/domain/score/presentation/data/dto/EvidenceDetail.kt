package com.team.incube.gsmc.v3.domain.score.presentation.data.dto

import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import io.swagger.v3.oas.annotations.media.Schema

data class EvidenceDetail(
    @param:Schema(description = "증거자료 ID", example = "1")
    val evidenceId: Long,
    @param:Schema(description = "증거자료 제목", example = "정보처리기능사 자격증")
    val title: String,
    @param:Schema(description = "증거자료 내용", example = "2024년 상반기 취득")
    val content: String,
    @param:Schema(description = "첨부 파일 목록")
    val files: List<FileItem>,
)
