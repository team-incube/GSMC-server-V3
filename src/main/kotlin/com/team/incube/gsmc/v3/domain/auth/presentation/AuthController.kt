package com.team.incube.gsmc.v3.domain.auth.presentation

import com.team.incube.gsmc.v3.domain.auth.presentation.data.request.OAuthCodeRequest
import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import com.team.incube.gsmc.v3.domain.auth.service.OauthAuthenticationService
import com.team.incube.gsmc.v3.domain.auth.service.TokenRefreshService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth API", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v3/auth")
class AuthController(
    private val oauthAuthenticationService: OauthAuthenticationService,
    private val tokenRefreshService: TokenRefreshService,
) {
    @Operation(summary = "OAuth 인증", description = "Authentication Code를 통해 OAuth 인증을 처리합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OAuth 인증 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "OAuth 인증에 실패함",
                content = [Content()],
            ),
        ],
    )
    @PostMapping
    fun oauthAuthentication(
        @Valid @RequestBody request: OAuthCodeRequest,
    ): AuthTokenResponse = oauthAuthenticationService.execute(request.code)

    @Operation(summary = "JWT 토큰 재발급", description = "RefreshToken을 이용하여 JWT 토큰을 재발급합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 재발급 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT 인증 실패",
                content = [Content()],
            ),
        ],
    )
    @PutMapping("/refresh")
    fun tokenRefresh(
        @Parameter(name = "refreshToken", `in` = ParameterIn.COOKIE, required = true, description = "Refresh token cookie")
        @CookieValue("refreshToken") refreshToken: String,
    ): AuthTokenResponse = tokenRefreshService.execute(refreshToken)
}
