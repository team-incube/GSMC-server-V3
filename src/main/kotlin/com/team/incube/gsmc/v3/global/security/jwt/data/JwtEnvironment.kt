package com.team.incube.gsmc.v3.global.security.jwt.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.jwt")
data class JwtEnvironment(
    val issuer: String,
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
) {
    data class AccessToken(
        val secret: String,
        val expiration: Long,
    )

    data class RefreshToken(
        val secret: String,
        val expiration: Long,
    )
}
