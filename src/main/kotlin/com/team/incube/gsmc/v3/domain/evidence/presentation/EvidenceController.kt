package com.team.incube.gsmc.v3.domain.evidence.presentation

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.CreateEvidenceRequest
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.request.PatchEvidenceRequest
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.CreateEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.PatchEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceService
import com.team.incube.gsmc.v3.domain.evidence.service.DeleteEvidenceService
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceByIdService
import com.team.incube.gsmc.v3.domain.evidence.service.UpdateEvidenceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Evidence API", description = "증빙자료 관리 API")
@RestController
@RequestMapping("/api/v3/evidences")
class EvidenceController(
    private val findEvidenceByIdService: FindEvidenceByIdService,
    private val createEvidenceService: CreateEvidenceService,
    private val updateEvidenceService: UpdateEvidenceService,
    private val deleteEvidenceService: DeleteEvidenceService,
) {
    @Operation(summary = "증빙자료 단건 조회", description = "ID를 통해 증빙자료를 조회합니다")
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
                content = [Content()],
            ),
        ],
    )
    @GetMapping("/{evidenceId}")
    fun getEvidence(
        @PathVariable evidenceId: Long,
    ): GetEvidenceResponse = findEvidenceByIdService.execute(evidenceId)

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
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 증빙을 가진 점수가 포함됨",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    fun createEvidence(
        @Valid @RequestBody request: CreateEvidenceRequest,
    ): CreateEvidenceResponse =
        createEvidenceService.execute(
            scoreIds = request.scoreIds,
            title = request.title,
            content = request.content,
            fileIds = request.fileId,
        )

    @Operation(summary = "증빙자료 수정", description = "기존 증빙자료를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "증빙자료 수정 성공",
                content = [Content(schema = Schema(implementation = PatchEvidenceResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 참가자를 매핑함 또는 존재하지 않는 증빙자료를 사용함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{evidenceId}")
    fun patchEvidence(
        @PathVariable evidenceId: Long,
        @Valid @RequestBody request: PatchEvidenceRequest,
    ): PatchEvidenceResponse =
        updateEvidenceService.execute(
            evidenceId = evidenceId,
            participants = request.participants,
            title = request.title,
            content = request.content,
            fileIds = request.fileId,
        )

    @Operation(summary = "증빙자료 삭제", description = "기존 증빙자료를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "증빙자료 삭제 성공",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 증빙자료를 매핑함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{evidenceId}")
    fun deleteEvidence(
        @PathVariable evidenceId: Long,
    ) {
        deleteEvidenceService.execute(evidenceId)
    }
}
