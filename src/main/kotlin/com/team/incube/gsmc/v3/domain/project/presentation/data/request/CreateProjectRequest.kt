package com.team.incube.gsmc.v3.domain.project.presentation.data.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateProjectRequest(
    @param:Schema(description = "프로젝트 제목", example = "스마트팜 IoT 시스템")
    @field:NotBlank(message = "프로젝트 제목은 필수입니다")
    @field:Size(min = 1, max = 100, message = "프로젝트 제목은 최소 1자, 최대 100자까지 가능합니다")
    val title: String,
    @param:Schema(description = "프로젝트 설명", example = "라즈베리파이를 활용한 스마트팜 자동화 시스템")
    @field:NotBlank(message = "프로젝트 설명은 필수입니다")
    @field:Size(min = 300, max = 2000, message = "프로젝트 설명은 최소 300자, 최대 2000자까지 가능합니다")
    val description: String,
    @param:Schema(description = "파일 ID 목록", example = "[1, 2, 3]")
    val fileIds: List<Long> = emptyList(),
    @param:Schema(description = "참가자 ID 목록", example = "[1, 2, 3]")
    @field:NotEmpty
    val participantIds: List<Long>,
)
