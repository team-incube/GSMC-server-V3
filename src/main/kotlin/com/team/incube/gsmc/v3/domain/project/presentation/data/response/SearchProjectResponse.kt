package com.team.incube.gsmc.v3.domain.project.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

data class SearchProjectResponse(
    @param:Schema(description = "프로젝트 목록")
    val projects: List<ProjectResponse>,
    @param:Schema(description = "전체 프로젝트 수", example = "100")
    val total: Long,
    @param:Schema(description = "현재 페이지 번호", example = "0")
    val page: Int,
    @param:Schema(description = "페이지 크기", example = "10")
    val size: Int,
)
