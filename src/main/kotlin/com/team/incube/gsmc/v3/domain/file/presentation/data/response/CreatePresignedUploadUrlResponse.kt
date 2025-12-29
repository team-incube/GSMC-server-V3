package com.team.incube.gsmc.v3.domain.file.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Pre-signed URL 생성 응답")
data class CreatePresignedUploadUrlResponse(
    @Schema(description = "Pre-signed URL")
    val presignedUrl: String,
    @Schema(description = "파일 키 (S3 저장 경로)")
    val fileKey: String,
    @Schema(description = "URL 만료 시간")
    val expiresAt: Instant,
)
