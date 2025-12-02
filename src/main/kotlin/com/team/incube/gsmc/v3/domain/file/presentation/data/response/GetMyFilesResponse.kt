package com.team.incube.gsmc.v3.domain.file.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

data class GetMyFilesResponse(
    @param:Schema(description = "파일 목록")
    val files: List<GetFileResponse>,
)
