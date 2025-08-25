package com.team.incube.gsmc.v3.global.security.config

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component

@Component
class DomainAuthorizationConfig {
    fun configure(authorizeRequests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        authorizeRequests
            /* Swagger */
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            /* Health Check */
            .requestMatchers("/api/v3/health").permitAll()
            /* Others */
            .anyRequest().permitAll()
    }
}