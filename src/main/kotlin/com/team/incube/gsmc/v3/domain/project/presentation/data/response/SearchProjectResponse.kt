package com.team.incube.gsmc.v3.domain.project.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

data class SearchProjectResponse(
    @param:Schema(description = "프로젝트 목록")
    val projects: List<ProjectResponse>,
    @param:Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,
    @param:Schema(description = "전체 요소 수", example = "100")
    val totalElements: Long,
)
