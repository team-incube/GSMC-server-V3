package com.team.incube.gsmc.v3.domain.archive.presentation

import com.team.incube.gsmc.v3.domain.archive.presentation.data.response.ArchiveScoresResponse
import com.team.incube.gsmc.v3.domain.archive.presentation.data.response.GetArchivedScoresResponse
import com.team.incube.gsmc.v3.domain.archive.service.ArchiveScoresByYearService
import com.team.incube.gsmc.v3.domain.archive.service.FindArchivedScoresByYearService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Archive API", description = "인증제 점수 아카이브 관련 API")
@RestController
@RequestMapping("/api/v3/archive")
class ArchiveController(
    private val archiveScoresByYearService: ArchiveScoresByYearService,
    private val findArchivedScoresByYearService: FindArchivedScoresByYearService,
) {
    @Operation(
        summary = "학년도별 인증제 점수 아카이브",
        description =
            "특정 학년도의 누적 인증제 항목 점수를 아카이브합니다. 기존 학년도 데이터를 완전히 덮어씁니다",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "아카이브 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "아카이브할 인증제 점수가 없음",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    fun archiveScores(
        @RequestParam academicYear: Int,
    ): ArchiveScoresResponse {
        val count = archiveScoresByYearService.execute(academicYear)
        return ArchiveScoresResponse(
            academicYear = academicYear,
            archivedCount = count,
            message = "${academicYear}학년도 인증제 점수 ${count}개가 아카이브되었습니다.",
        )
    }

    @Operation(
        summary = "학년도별 아카이브 조회",
        description = "특정 학년도의 아카이브된 인증제 점수를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 학년도의 아카이브를 찾을 수 없음",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    fun getArchivedScores(
        @RequestParam academicYear: Int,
    ): GetArchivedScoresResponse {
        val archives = findArchivedScoresByYearService.execute(academicYear)
        return GetArchivedScoresResponse(
            academicYear = academicYear,
            totalCount = archives.size,
            archives = archives,
        )
    }
}
