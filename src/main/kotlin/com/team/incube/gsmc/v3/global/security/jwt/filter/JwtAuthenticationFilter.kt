package com.team.incube.gsmc.v3.global.security.jwt.filter

import com.team.incube.gsmc.v3.global.security.jwt.JwtParser
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class JwtAuthenticationFilter(
    private val jwtParser: JwtParser,
) : OncePerRequestFilter() {
    private val pathMatcher = AntPathMatcher()

    companion object {
        private val EXCLUDED_PATHS =
            listOf(
                "/api/v3/auth/**",
                "/actuator/prometheus/**",
                "/api/v3/health/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-ui.html",
            )
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return EXCLUDED_PATHS.any { pattern -> pathMatcher.match(pattern, uri) }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = jwtParser.resolveToken(request)

        if (token != null && jwtParser.validateAccessToken(token)) {
            val userId = jwtParser.getUserIdFromAccessToken(token)
            val role = jwtParser.getRoleFromAccessToken(token)
            val authorities: List<GrantedAuthority> = listOf(SimpleGrantedAuthority(role.name))
            val authentication =
                UsernamePasswordAuthenticationToken(userId, null, authorities)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        }

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("{\"message\": \"Unauthorized or invalid token.\"}")
    }
}
