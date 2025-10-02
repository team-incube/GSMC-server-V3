package com.team.incube.gsmc.v3.domain.evidence.presentation

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.service.FindEvidenceByIdService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/evidences")
class EvidenceController(
    private val findEvidenceByIdService: FindEvidenceByIdService,
) {
    @GetMapping("/{evidenceId}")
    fun getEvidence(
        @PathVariable evidenceId: Long,
    ): CommonApiResponse<GetEvidenceResponse> {
        val evidence = findEvidenceByIdService.execute(evidenceId)
        return CommonApiResponse.success("증빙자료 조회가 완료되었습니다.", evidence)
    }
}
