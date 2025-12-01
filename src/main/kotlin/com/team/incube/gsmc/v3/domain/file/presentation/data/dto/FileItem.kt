package com.team.incube.gsmc.v3.domain.file.presentation.data.dto

import io.swagger.v3.oas.annotations.media.Schema

data class FileItem(
    @param:Schema(description = "파일 ID", example = "1")
    val id: Long,
    @param:Schema(description = "회원 ID", example = "1")
    val member: Long,
    @param:Schema(description = "원본 파일명", example = "certificate.pdf")
    val originalName: String,
    @param:Schema(description = "저장 파일명", example = "abc123.pdf")
    val storeName: String,
    @param:Schema(description = "파일 URI", example = "https://storage.example.com/abc123.pdf")
    val uri: String,
)
