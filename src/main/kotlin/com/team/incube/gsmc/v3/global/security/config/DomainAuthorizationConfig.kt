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
            .hasRole(MemberRole.UNAUTHORIZED.name)
            .requestMatchers("/api/v3/auth/teacher-signup")
            .hasRole(MemberRole.UNAUTHORIZED.name)
            .requestMatchers("/api/v3/auth/teacher-signup/requests")
            .hasAnyRole(MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            .requestMatchers("/api/v3/auth/teacher-signup/*/approve", "/api/v3/auth/teacher-signup/*/reject")
            .hasAnyRole(MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            .requestMatchers("/api/v3/auth/**")
            .permitAll()
            // Developer
            .requestMatchers("/api/v3/developer/**")
            .permitAll()
            // Alert
            .requestMatchers("/api/v3/alerts/**")
            .hasAnyRole(MemberRole.STUDENT.name, MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            // Category
            .requestMatchers("/api/v3/categories/**")
            .hasRole(MemberRole.STUDENT.name)
            // Evidence
            .requestMatchers(HttpMethod.GET, "/api/v3/evidences/my", "/api/v3/evidences/draft")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers(HttpMethod.POST, "/api/v3/evidences/draft")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers(HttpMethod.DELETE, "/api/v3/evidences/draft")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers("/api/v3/evidences/**")
            .hasAnyRole(MemberRole.STUDENT.name, MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            // File
            .requestMatchers(HttpMethod.GET, "/api/v3/files/my", "/api/v3/files/my/unused")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers("/api/v3/files/**")
            .hasAnyRole(MemberRole.STUDENT.name, MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            // Member
            .requestMatchers("/api/v3/members/**")
            .hasAnyRole(MemberRole.STUDENT.name, MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            // Project
            .requestMatchers(HttpMethod.GET, "/api/v3/projects/draft", "/api/v3/projects/*/my-score-and-evidence")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers(HttpMethod.POST, "/api/v3/projects/draft")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers(HttpMethod.DELETE, "/api/v3/projects/draft")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers("/api/v3/projects/**")
            .hasAnyRole(MemberRole.STUDENT.name, MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            // Score
            .requestMatchers(HttpMethod.PUT, "/api/v3/scores/*/status")
            .hasAnyRole(MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            .requestMatchers(HttpMethod.PATCH, "/api/v3/scores/*/approve", "/api/v3/scores/*/reject")
            .hasAnyRole(MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            .requestMatchers(HttpMethod.GET, "/api/v3/scores/total/*", "/api/v3/scores/by-category/*")
            .hasAnyRole(MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            .requestMatchers(HttpMethod.GET, "/api/v3/scores", "/api/v3/scores/by-category", "/api/v3/scores/total")
            .hasRole(MemberRole.STUDENT.name)
            .requestMatchers(
                HttpMethod.POST,
                "/api/v3/scores/certificate",
                "/api/v3/scores/award",
                "/api/v3/scores/topcit",
                "/api/v3/scores/toeic",
                "/api/v3/scores/toeic-academy",
                "/api/v3/scores/jlpt",
                "/api/v3/scores/read-a-thon",
                "/api/v3/scores/volunteer",
                "/api/v3/scores/ncs",
                "/api/v3/scores/newrrow-school",
                "/api/v3/scores/academic-grade",
                "/api/v3/scores/external-activity",
                "/api/v3/scores/project-participation",
            ).hasRole(MemberRole.STUDENT.name)
            .requestMatchers("/api/v3/scores/**")
            .hasAnyRole(MemberRole.STUDENT.name, MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            // Sheet
            .requestMatchers("/api/v3/sheets/**")
            .hasAnyRole(MemberRole.TEACHER.name, MemberRole.HOMEROOM_TEACHER.name, MemberRole.ROOT.name)
            .anyRequest()
            .authenticated()
    }
}
