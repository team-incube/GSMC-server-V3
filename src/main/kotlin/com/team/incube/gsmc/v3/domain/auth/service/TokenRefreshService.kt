package com.team.incube.gsmc.v3.domain.auth.service

import com.team.incube.gsmc.v3.domain.auth.dto.TokenPair

interface TokenRefreshService {
    fun execute(refreshToken: String): TokenPair
}
