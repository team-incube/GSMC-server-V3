package com.team.incube.gsmc.v3.domain.alert.service

import com.team.incube.gsmc.v3.domain.alert.presentation.data.response.GetMyAlertsResponse

interface FindMyAlertsService {
    fun execute(): GetMyAlertsResponse
}
