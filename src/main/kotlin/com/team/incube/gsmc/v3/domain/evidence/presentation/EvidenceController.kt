package com.team.incube.gsmc.v3.domain.evidence.presentation

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceByIdService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Evidence API", description = "증빙자료 관련 API입니다.")
@RequestMapping("/api/v3/evidences")
class EvidenceController(
    private val findEvidenceByIdService: FindEvidenceByIdService,
) {
    @Operation(summary = "증빙자료 단건조회", description = "증빙자료를 단건 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "증빙자료 단건조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당하는 증빙자료를 찾을 수 없습니다.",
                content = [Content()],
            ),
        ],
    )
    @GetMapping("/{evidenceId}")
    fun getEvidence(
        @Schema(description = "증빙자료 ID") @PathVariable(value = "evidenceId") evidenceId: Long,
    ): GetEvidenceResponse = findEvidenceByIdService.execute(evidenceId)
}
