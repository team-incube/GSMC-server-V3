package com.team.incube.gsmc.v3.global.common.cookie

import org.springframework.core.env.Environment
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CookieUtil(
    private val environment: Environment,
) {
    companion object {
        private const val ACCESS_TOKEN_COOKIE_NAME = "accessToken"
        private const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
    }

    private val isProduction: Boolean
        get() = environment.activeProfiles.contains("prod")

    fun createAccessTokenCookie(
        token: String,
        maxAge: Duration,
    ): ResponseCookie =
        ResponseCookie
            .from(ACCESS_TOKEN_COOKIE_NAME, token)
            .httpOnly(true)
            .secure(isProduction)
            .sameSite("Strict")
            .path("/")
            .maxAge(maxAge)
            .build()

    fun createRefreshTokenCookie(
        token: String,
        maxAge: Duration,
    ): ResponseCookie =
        ResponseCookie
            .from(REFRESH_TOKEN_COOKIE_NAME, token)
            .httpOnly(true)
            .secure(isProduction)
            .sameSite("Strict")
            .path("/")
            .maxAge(maxAge)
            .build()

    fun deleteAccessTokenCookie(): ResponseCookie =
        ResponseCookie
            .from(ACCESS_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(isProduction)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build()

    fun deleteRefreshTokenCookie(): ResponseCookie =
        ResponseCookie
            .from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(isProduction)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build()
}
