package com.team.incube.gsmc.v3.global.security.config

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component

@Component
class DomainAuthorizationConfig {
    fun configure(authorizeRequests: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        authorizeRequests
            // Swagger & Health Check
            .requestMatchers("/swagger-ui/**", "/api-docs/**")
            .permitAll()
            .requestMatchers("/api/v3/health")
            .permitAll()
            // Auth
            .requestMatchers("/api/v3/auth/signup")
            .hasRole("UNAUTHORIZED")
            .requestMatchers("/api/v3/auth/**")
            .permitAll()
            // Developer
            .requestMatchers("/api/v3/developer/**")
            .permitAll()
            // Alert
            .requestMatchers("/api/v3/alerts/**")
            .hasAnyRole("STUDENT", "TEACHER", "HOMEROOM_TEACHER", "ROOT")
            // Category
            .requestMatchers("/api/v3/categories/**")
            .hasRole("STUDENT")
            // Evidence
            .requestMatchers(HttpMethod.GET, "/api/v3/evidences/my", "/api/v3/evidences/draft")
            .hasRole("STUDENT")
            .requestMatchers(HttpMethod.POST, "/api/v3/evidences/draft")
            .hasRole("STUDENT")
            .requestMatchers(HttpMethod.DELETE, "/api/v3/evidences/draft")
            .hasRole("STUDENT")
            .requestMatchers("/api/v3/evidences/**")
            .hasAnyRole("STUDENT", "TEACHER", "HOMEROOM_TEACHER", "ROOT")
            // File
            .requestMatchers(HttpMethod.GET, "/api/v3/files/my", "/api/v3/files/my/unused")
            .hasRole("STUDENT")
            .requestMatchers("/api/v3/files/**")
            .hasAnyRole("STUDENT", "TEACHER", "HOMEROOM_TEACHER", "ROOT")
            // Member
            .requestMatchers("/api/v3/members/**")
            .hasAnyRole("STUDENT", "TEACHER", "HOMEROOM_TEACHER", "ROOT")
            // Project
            .requestMatchers(HttpMethod.GET, "/api/v3/projects/draft", "/api/v3/projects/*/my-score-and-evidence")
            .hasRole("STUDENT")
            .requestMatchers(HttpMethod.POST, "/api/v3/projects/draft")
            .hasRole("STUDENT")
            .requestMatchers(HttpMethod.DELETE, "/api/v3/projects/draft")
            .hasRole("STUDENT")
            .requestMatchers("/api/v3/projects/**")
            .hasAnyRole("STUDENT", "TEACHER", "HOMEROOM_TEACHER", "ROOT")
            // Score
            .requestMatchers(HttpMethod.POST, "/api/v3/scores/volunteer", "/api/v3/scores/academic-grade")
            .hasAnyRole("TEACHER", "HOMEROOM_TEACHER", "ROOT")
            .requestMatchers(HttpMethod.PUT, "/api/v3/scores/*/status")
            .hasAnyRole("TEACHER", "HOMEROOM_TEACHER", "ROOT")
            .requestMatchers(HttpMethod.PATCH, "/api/v3/scores/*/approve", "/api/v3/scores/*/reject")
            .hasAnyRole("TEACHER", "HOMEROOM_TEACHER", "ROOT")
            .requestMatchers(HttpMethod.GET, "/api/v3/scores/total/*", "/api/v3/scores/by-category/*")
            .hasAnyRole("TEACHER", "HOMEROOM_TEACHER", "ROOT")
            .requestMatchers(HttpMethod.GET, "/api/v3/scores", "/api/v3/scores/by-category", "/api/v3/scores/total")
            .hasRole("STUDENT")
            .requestMatchers(
                HttpMethod.POST,
                "/api/v3/scores/certificate",
                "/api/v3/scores/award",
                "/api/v3/scores/topcit",
                "/api/v3/scores/toeic",
                "/api/v3/scores/toeic-academy",
                "/api/v3/scores/jlpt",
                "/api/v3/scores/read-a-thon",
                "/api/v3/scores/ncs",
                "/api/v3/scores/newrrow-school",
                "/api/v3/scores/external-activity",
                "/api/v3/scores/project-participation",
            ).hasRole("STUDENT")
            .requestMatchers("/api/v3/scores/**")
            .hasAnyRole("STUDENT", "TEACHER", "HOMEROOM_TEACHER", "ROOT")
            // Sheet
            .requestMatchers("/api/v3/sheets/**")
            .hasAnyRole("TEACHER", "HOMEROOM_TEACHER", "ROOT")
            .anyRequest()
            .authenticated()
    }
}
