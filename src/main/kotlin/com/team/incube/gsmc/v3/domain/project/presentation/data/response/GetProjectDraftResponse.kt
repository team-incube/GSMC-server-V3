package com.team.incube.gsmc.v3.domain.project.presentation.data.response

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.member.dto.Member
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "프로젝트 임시저장 조회 응답")
data class GetProjectDraftResponse(
    @param:Schema(description = "프로젝트 제목", example = "스마트팜 IoT 시스템")
    val title: String,
    @param:Schema(description = "프로젝트 설명", example = "라즈베리파이를 활용한 스마트팜 자동화 시스템")
    val description: String,
    @param:Schema(description = "파일 목록")
    val files: List<GetFileResponse>,
    @param:Schema(description = "참가자 목록")
    val participants: List<Member>,
)
