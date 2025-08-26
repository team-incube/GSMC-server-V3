package com.team.incube.gsmc.v3.global.security.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.cors")
data class CorsEnvironment(
    val allowedOrigins: List<String> = listOf("*"),
)
