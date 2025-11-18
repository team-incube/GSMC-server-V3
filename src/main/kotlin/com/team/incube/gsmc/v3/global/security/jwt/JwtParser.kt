package com.team.incube.gsmc.v3.global.security.jwt

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.data.JwtEnvironment
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class JwtParser(
    private val jwtEnvironment: JwtEnvironment,
) {
    private lateinit var accessTokenKey: javax.crypto.SecretKey
    private lateinit var refreshTokenKey: javax.crypto.SecretKey

    @PostConstruct
    fun init() {
        accessTokenKey = Keys.hmacShaKeyFor(jwtEnvironment.accessToken.secret.toByteArray())
        refreshTokenKey = Keys.hmacShaKeyFor(jwtEnvironment.refreshToken.secret.toByteArray())
    }

    fun validateAccessToken(token: String): Boolean =
        try {
            parseAccessTokenClaims(token)
            true
        } catch (e: Exception) {
            false
        }

    fun validateRefreshToken(token: String): Boolean =
        try {
            parseRefreshTokenClaims(token)
            true
        } catch (e: Exception) {
            false
        }

    fun getUserIdFromAccessToken(token: String): String = parseAccessTokenClaims(token).subject

    fun getUserIdFromRefreshToken(token: String): String = parseRefreshTokenClaims(token).subject

    fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        val trim = header.trim()
        if (!trim.startsWith("Bearer ", ignoreCase = true)) return null
        val token = trim.substring(7).trim()
        return token.ifEmpty { null }
    }

    fun getRoleFromAccessToken(token: String): MemberRole =
        MemberRole.valueOf(parseAccessTokenClaims(token).get("role", String::class.java))

    private fun parseAccessTokenClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(accessTokenKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()

    private fun parseRefreshTokenClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(refreshTokenKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
}
