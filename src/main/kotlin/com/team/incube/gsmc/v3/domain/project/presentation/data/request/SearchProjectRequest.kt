package com.team.incube.gsmc.v3.domain.project.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema

data class SearchProjectRequest(
    @param:Schema(description = "검색할 프로젝트 제목", example = "스마트팜", required = false)
    val title: String? = null,
    @param:Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    val page: Int = 0,
    @param:Schema(description = "페이지 크기", example = "10", defaultValue = "10")
    val size: Int = 10,
)
