package com.team.incube.gsmc.v3.domain.score.presentation

import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateCertificateScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.UpdateScoreStatusRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.service.CreateCertificateScoreService
import com.team.incube.gsmc.v3.domain.score.service.DeleteScoreService
import com.team.incube.gsmc.v3.domain.score.service.UpdateScoreStatusService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Score API", description = "인증제 점수 관련 API")
@RestController
@RequestMapping("/api/v3/scores")
class ScoreController(
    private val updateScoreStatusService: UpdateScoreStatusService,
    private val deleteScoreService: DeleteScoreService,
    private val createCertificateScoreService: CreateCertificateScoreService,
) {
    @Operation(summary = "인증제 점수 상태 업데이트", description = "인증제 점수의 승인/거절 상태를 업데이트합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 인증제 점수를 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{scoreId}/status")
    fun updateEvidenceStatus(
        @PathVariable scoreId: Long,
        @Valid @RequestBody request: UpdateScoreStatusRequest,
    ): CommonApiResponse<Nothing> {
        updateScoreStatusService.execute(
            scoreId = scoreId,
            scoreStatus = request.scoreStatus,
        )
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "인증제 점수 삭제", description = "인증제 점수 및 연관된 증빙자료와 파일을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 점수를 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{scoreId}")
    fun deleteScore(
        @PathVariable scoreId: Long,
    ): CommonApiResponse<Nothing> {
        deleteScoreService.execute(scoreId = scoreId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "자격증 영역 인증제 점수 추가", description = "자격증 영역에 대한 인증제 점수를 추가합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 해당 영역에 대한 인증제 점수를 전부 취득함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/certificates")
    fun addCertificateScore(
        @RequestBody @Valid request: CreateCertificateScoreRequest,
    ): CreateScoreResponse =
        createCertificateScoreService.execute(
            certificateName = request.certificateName,
            fileId = request.fileId,
        )
}
