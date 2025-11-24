package com.team.incube.gsmc.v3.domain.alert.service

interface PatchAlertIsReadService {
    fun execute(lastAlertId: Long)
}
