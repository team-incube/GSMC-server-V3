package com.team.incube.gsmc.v3.domain.file.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "파일 업로드 확인 요청")
data class ConfirmFileUploadRequest(
    @Schema(description = "파일 키 (Pre-signed URL 생성 시 받은 fileKey)", example = "file/20250101120000_abc123.pdf")
    @field:NotBlank(message = "파일 키는 필수입니다")
    val fileKey: String,
    @Schema(description = "원본 파일명", example = "example.pdf")
    @field:NotBlank(message = "원본 파일명은 필수입니다")
    val originalFileName: String,
)
