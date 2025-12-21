package com.team.incube.gsmc.v3.domain.auth.service

import com.team.incube.gsmc.v3.domain.auth.dto.TokenPair

interface OAuthAuthenticationService {
    fun execute(code: String): TokenPair
}
