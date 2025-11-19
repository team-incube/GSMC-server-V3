package com.team.incube.gsmc.v3.domain.member.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 검색 응답")
data class SearchMemberResponse(
    @param:Schema(description = "전체 페이지 수", example = "10")
    val totalPages: Int,
    @param:Schema(description = "전체 요소 수", example = "100")
    val totalElements: Long,
    @param:Schema(description = "사용자 목록")
    val members: List<GetMemberResponse>,
)
