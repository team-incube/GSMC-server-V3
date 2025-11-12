package com.team.incube.gsmc.v3.global.security.oauth.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
data class OauthEnvironment(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
)
