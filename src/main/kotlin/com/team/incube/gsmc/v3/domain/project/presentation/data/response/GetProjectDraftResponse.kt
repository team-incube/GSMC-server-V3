package com.team.incube.gsmc.v3.domain.project.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable

@Schema(description = "프로젝트 임시저장 조회 응답")
data class GetProjectDraftResponse(
    @field:Schema(description = "프로젝트 제목", example = "스마트팜 IoT 시스템")
    val title: String,
    @field:Schema(description = "프로젝트 설명", example = "라즈베리파이를 활용한 스마트팜 자동화 시스템")
    val description: String,
    @field:Schema(description = "파일 ID 목록", example = "[1, 2, 3]")
    val fileIds: List<Long>,
    @field:Schema(description = "참가자 ID 목록", example = "[1, 2, 3]")
    val participantIds: List<Long>,
) : Serializable
