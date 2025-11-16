package com.team.incube.gsmc.v3.domain.file.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

data class GetMyFilesResponse(
    @param:Schema(description = "파일 목록")
    val files: List<FileItem>,
)

data class FileItem(
    @param:Schema(description = "파일 ID", example = "1")
    val fileId: Long,
    @param:Schema(description = "원본 파일명", example = "certificate.pdf")
    val originalName: String,
    @param:Schema(description = "저장 파일명", example = "abc123.pdf")
    val storedName: String,
    @param:Schema(description = "파일 URI", example = "https://storage.example.com/abc123.pdf")
    val uri: String,
)