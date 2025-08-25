package com.team.incube.gsmc.v3.global.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(
    @Value("\${security.cors.allowed-origins}") private val allowedOrigins: List<String>
) {
    @Bean
    fun configure(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            this.allowedOrigins = this@CorsConfig.allowedOrigins
            this.allowedMethods = HttpMethod.values().map(HttpMethod::name)
            this.addAllowedHeader("*")
            this.allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}