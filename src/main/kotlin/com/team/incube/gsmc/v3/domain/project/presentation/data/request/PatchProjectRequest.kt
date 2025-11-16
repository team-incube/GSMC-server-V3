package com.team.incube.gsmc.v3.domain.project.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema

data class PatchProjectRequest(
    @param:Schema(description = "프로젝트 제목", example = "스마트팜 IoT 시스템 v2", nullable = true)
    val title: String? = null,
    @param:Schema(description = "프로젝트 설명", example = "업데이트된 스마트팜 자동화 시스템", nullable = true)
    val description: String? = null,
    @param:Schema(description = "파일 ID 목록", example = "[1, 2, 3]", nullable = true)
    val fileIds: List<Long>? = null,
    @param:Schema(description = "참가자 ID 목록", example = "[1, 2, 3]", nullable = true)
    val participantIds: List<Long>? = null,
)
