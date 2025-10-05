package com.team.incube.gsmc.v3.domain.file.presentation.data.response

import com.team.incube.gsmc.v3.domain.file.dto.File
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "파일 생성 응답")
data class CreateFileResponse(
    @field:Schema(description = "파일 ID", example = "1")
    val id: Long,

    @field:Schema(description = "증빙자료 ID (선택적)", example = "1")
    val evidenceId: Long? = null,

    @field:Schema(description = "파일 원본명", example = "증빙자료.pdf")
    val fileOriginalName: String,

    @field:Schema(description = "파일 저장명", example = "20251005143022_a1b2c3d4e5f6.pdf")
    val fileStoredName: String,

    @field:Schema(description = "파일 URI", example = "https://gsmc-bucket.s3.amazonaws.com/evidences/20251005143022_a1b2c3d4e5f6.pdf")
    val fileUri: String,
) {
    companion object {
        fun from(file: File, evidenceId: Long? = null) = CreateFileResponse(
            id = file.fileId ?: 0,
            evidenceId = evidenceId,
            fileOriginalName = file.fileOriginalName ?: "",
            fileStoredName = file.fileStoredName ?: "",
            fileUri = file.fileUri ?: ""
        )
    }
}
