package com.team.incube.gsmc.v3.domain.sheet.presentation.controller

import com.team.incube.gsmc.v3.domain.sheet.service.CreateClassScoreSheetService
import com.team.incube.gsmc.v3.domain.sheet.service.CreateGradeScoreSheetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Sheet API", description = "엑셀 시트 생성 API")
@RestController
@RequestMapping("/api/v3/sheets")
class SheetController(
    private val createClassScoreSheetService: CreateClassScoreSheetService,
    private val createGradeScoreSheetService: CreateGradeScoreSheetService,
) {
    @Operation(
        summary = "학급별 학생 점수 현황 엑셀 다운로드",
        description = "지정된 학년과 반의 모든 학생들의 인증제 점수 현황을 엑셀 파일로 생성합니다",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "엑셀 파일 생성 성공",
                content = [Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (학년 또는 반 번호 오류)",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/class-scores")
    fun downloadClassScoreSheet(
        @RequestParam @Valid @Max(value = 3) @Min(value = 1) grade: Int,
        @RequestParam @Valid @Max(value = 4) @Min(value = 1) classNumber: Int,
    ): ResponseEntity<ByteArrayResource> = createClassScoreSheetService.execute(grade = grade, classNumber = classNumber)

    @Operation(
        summary = "학년별 학생 점수 현황 엑셀 다운로드",
        description = "지정된 학년의 모든 학생들의 인증제 점수 현황을 엑셀 파일로 생성합니다",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "엑셀 파일 생성 성공",
                content = [Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (학년 번호 오류)",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/grade-scores")
    fun downloadGradeScoreSheet(
        @RequestParam @Valid @Max(value = 3) @Min(value = 1) grade: Int,
    ): ResponseEntity<ByteArrayResource> = createGradeScoreSheetService.execute(grade = grade)
}
