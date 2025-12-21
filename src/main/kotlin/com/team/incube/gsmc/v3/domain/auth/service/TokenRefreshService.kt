package com.team.incube.gsmc.v3.domain.auth.service

import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import jakarta.servlet.http.HttpServletResponse

interface TokenRefreshService {
    fun execute(
        refreshToken: String,
        response: HttpServletResponse,
    ): AuthTokenResponse
}
