package com.team.incube.gsmc.v3.domain.file.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Pre-signed URL 생성 요청")
data class CreatePresignedUploadUrlRequest(
    @Schema(description = "파일명", example = "example.pdf")
    @field:NotBlank(message = "파일명은 필수입니다")
    val fileName: String,
    @Schema(description = "파일 크기 (bytes)", example = "1048576")
    @field:NotNull(message = "파일 크기는 필수입니다")
    @field:Positive(message = "파일 크기는 양수여야 합니다")
    val fileSize: Long,
    @Schema(description = "Content Type", example = "application/pdf")
    @field:NotBlank(message = "Content Type은 필수입니다")
    val contentType: String,
)
