package com.team.incube.gsmc.v3.domain.alert.presentation.data.response

import io.swagger.v3.oas.annotations.media.Schema

data class GetMyAlertsResponse(
    @field:Schema(description = "알림 목록")
    val alerts: List<GetAlertResponse>,
)
