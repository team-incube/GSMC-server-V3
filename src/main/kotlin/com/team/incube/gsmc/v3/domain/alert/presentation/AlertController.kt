package com.team.incube.gsmc.v3.domain.alert.presentation

import com.team.incube.gsmc.v3.domain.alert.presentation.data.request.PatchAlertIsReadRequest
import com.team.incube.gsmc.v3.domain.alert.presentation.data.response.GetMyAlertsResponse
import com.team.incube.gsmc.v3.domain.alert.service.DeleteAlertService
import com.team.incube.gsmc.v3.domain.alert.service.FindMyAlertsService
import com.team.incube.gsmc.v3.domain.alert.service.PatchAlertIsReadService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Alert API", description = "알림 관리 API")
@RestController
@RequestMapping("/api/v3/alerts")
class AlertController(
    private val findMyAlertsService: FindMyAlertsService,
    private val patchAlertIsReadService: PatchAlertIsReadService,
    private val deleteAlertService: DeleteAlertService,
) {
    @Operation(summary = "내 알림 목록 조회", description = "현재 인증된 사용자의 모든 알림을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "알림 목록 조회 성공",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    fun getMyAlerts(): GetMyAlertsResponse = findMyAlertsService.execute()

    @Operation(summary = "알림 읽음 처리", description = "lastAlertId까지의 알림을 읽음 처리합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "알림 읽음 처리 성공",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/read")
    fun patchAlertIsRead(
        @Valid @RequestBody request: PatchAlertIsReadRequest,
    ): CommonApiResponse<Nothing> {
        patchAlertIsReadService.execute(lastAlertId = request.lastAlertId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "알림 삭제 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "알림을 삭제할 권한이 없습니다.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 알림입니다.",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{alert_id}")
    fun deleteAlert(
        @PathVariable("alert_id") alertId: Long,
    ): CommonApiResponse<Nothing> {
        deleteAlertService.execute(alertId)
        return CommonApiResponse.success("OK")
    }
}
