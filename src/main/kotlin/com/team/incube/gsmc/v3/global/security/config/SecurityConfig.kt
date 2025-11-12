package com.team.incube.gsmc.v3.global.security.config

import com.team.incube.gsmc.v3.global.security.jwt.JwtParser
import com.team.incube.gsmc.v3.global.security.jwt.filter.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val domainAuthorizationConfig: DomainAuthorizationConfig,
    @param:Qualifier("configure") private val corsConfigurationSource: CorsConfigurationSource,
    private val jwtParser: JwtParser,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf(CsrfConfigurer<*>::disable)
            .cors { it.configurationSource(corsConfigurationSource) }
            .httpBasic(HttpBasicConfigurer<*>::disable)
            .formLogin(FormLoginConfigurer<*>::disable)
            .logout(LogoutConfigurer<*>::disable)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { domainAuthorizationConfig.configure(it) }
            .addFilterBefore(JwtAuthenticationFilter(jwtParser), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
