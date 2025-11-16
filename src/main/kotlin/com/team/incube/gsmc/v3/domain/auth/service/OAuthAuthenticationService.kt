package com.team.incube.gsmc.v3.domain.auth.service

import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse

interface OAuthAuthenticationService {
    fun execute(code: String): AuthTokenResponse
}
