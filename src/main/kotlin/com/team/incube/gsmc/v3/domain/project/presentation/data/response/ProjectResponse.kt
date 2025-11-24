package com.team.incube.gsmc.v3.domain.project.presentation.data.response

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.project.dto.ProjectParticipant
import io.swagger.v3.oas.annotations.media.Schema

data class ProjectResponse(
    @param:Schema(description = "프로젝트 ID", example = "1")
    val id: Long,
    @param:Schema(description = "프로젝트 소유자 ID", example = "1")
    val ownerId: Long,
    @param:Schema(description = "프로젝트 제목", example = "스마트팜 IoT 시스템")
    val title: String,
    @param:Schema(description = "프로젝트 설명", example = "라즈베리파이를 활용한 스마트팜 자동화 시스템")
    val description: String,
    @param:Schema(description = "프로젝트 관련 파일 목록")
    val files: List<File>,
    @param:Schema(description = "프로젝트 참가자 목록 (scoreId 포함)")
    val participants: List<ProjectParticipant>,
)
