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
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class JwtAuthenticationFilter(
    private val jwtParser: JwtParser,
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = jwtParser.resolveToken(request)

        if (token != null && jwtParser.validateAccessToken(token)) {
            val userId = jwtParser.getUserIdFromAccessToken(token).toLong()
            val role = jwtParser.getRoleFromAccessToken(token)
            val authorities: List<GrantedAuthority> = listOf(SimpleGrantedAuthority(role.name))
            val authentication =
                UsernamePasswordAuthenticationToken(userId, null, authorities)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}
