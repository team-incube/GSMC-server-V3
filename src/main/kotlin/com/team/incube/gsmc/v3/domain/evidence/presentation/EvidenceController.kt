package com.team.incube.gsmc.v3.domain.evidence.presentation

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.CreateEvidenceRequest
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.CreateEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceService
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceByIdService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "Evidence", description = "증빙자료 관리 API")
@RestController
@RequestMapping("/api/v3/evidences")
class EvidenceController(
    private val findEvidenceByIdService: FindEvidenceByIdService,
    private val createEvidenceService: CreateEvidenceService,
) {
    @Operation(summary = "증빙자료 조회", description = "ID를 통해 증빙자료를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "증빙자료 조회 성공",
                content = [Content(schema = Schema(implementation = GetEvidenceResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "증빙자료를 찾을 수 없음",
            ),
        ],
    )
    @GetMapping("/{evidenceId}")
    fun getEvidence(
        @PathVariable evidenceId: Long,
    ): CommonApiResponse<GetEvidenceResponse> {
        val evidence = findEvidenceByIdService.execute(evidenceId)
        return CommonApiResponse.success("증빙자료 조회가 완료되었습니다.", evidence)
    }

    @Operation(summary = "증빙자료 생성", description = "새로운 증빙자료를 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "증빙자료 생성 성공",
                content = [Content(schema = Schema(implementation = CreateEvidenceResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 점수 객체 또는 파일",
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 증빙을 가진 점수가 포함됨",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    fun createEvidence(
        @Valid @RequestBody request: CreateEvidenceRequest,
    ): CommonApiResponse<CreateEvidenceResponse> {
        val evidence =
            createEvidenceService.execute(
                scoreIds = request.scoreIds,
                title = request.title,
                content = request.content,
                fileIds = request.fileId,
            )

        val response =
            CreateEvidenceResponse(
                id = evidence.id,
                title = evidence.title,
                content = evidence.content,
                createAt = evidence.createdAt,
                updateAt = evidence.updatedAt,
                file = evidence.files,
            )

        return CommonApiResponse.created("증빙자료가 성공적으로 생성되었습니다.", response)
    }
}
