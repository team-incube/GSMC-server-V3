package com.team.incube.gsmc.v3.domain.file.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "파일 정보 응답")
data class GetFileResponse(
    @param:Schema(description = "파일 ID", example = "1")
    val id: Long,
    @param:Schema(description = "파일 소유자 ID", example = "1")
    val memberId: Long,
    @param:Schema(description = "원본 파일명", example = "certificate.pdf")
    val originalName: String,
    @param:Schema(description = "저장된 파일명", example = "20240101_123456_certificate.pdf")
    val storeName: String,
    @param:Schema(description = "파일 URI", example = "https://s3.amazonaws.com/bucket/files/20240101_123456_certificate.pdf")
    val uri: String,
)
