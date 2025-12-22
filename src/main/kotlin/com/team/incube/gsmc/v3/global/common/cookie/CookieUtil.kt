package com.team.incube.gsmc.v3.global.common.cookie

import org.springframework.core.env.Environment
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

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

    private fun buildCookie(
        name: String,
        value: String,
        maxAge: Duration,
    ): ResponseCookie {
        val cookieBuilder =
            ResponseCookie
                .from(name, value)
                .httpOnly(true)
                .secure(isProduction)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
        if (isProduction) {
            cookieBuilder.domain(".gsmc.io.kr")
        }
        return cookieBuilder.build()
    }

    fun createAuthCookies(
        accessToken: String,
        accessExpiration: LocalDateTime,
        refreshToken: String,
        refreshExpiration: LocalDateTime,
    ): Pair<ResponseCookie, ResponseCookie> {
        val accessMaxAge = Duration.between(LocalDateTime.now(), accessExpiration)
        val refreshMaxAge = Duration.between(LocalDateTime.now(), refreshExpiration)

        return createAccessTokenCookie(accessToken, accessMaxAge) to
            createRefreshTokenCookie(refreshToken, refreshMaxAge)
    }

    fun createAccessTokenCookie(
        token: String,
        maxAge: Duration,
    ): ResponseCookie = buildCookie(ACCESS_TOKEN_COOKIE_NAME, token, maxAge)

    fun createRefreshTokenCookie(
        token: String,
        maxAge: Duration,
    ): ResponseCookie = buildCookie(REFRESH_TOKEN_COOKIE_NAME, token, maxAge)

    fun deleteAccessTokenCookie(): ResponseCookie = buildCookie(ACCESS_TOKEN_COOKIE_NAME, "", Duration.ZERO)

    fun deleteRefreshTokenCookie(): ResponseCookie = buildCookie(REFRESH_TOKEN_COOKIE_NAME, "", Duration.ZERO)
}
