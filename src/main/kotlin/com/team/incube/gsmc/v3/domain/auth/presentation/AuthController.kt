package com.team.incube.gsmc.v3.domain.auth.presentation

import com.team.incube.gsmc.v3.domain.auth.presentation.data.request.OAuthCodeRequest
import com.team.incube.gsmc.v3.domain.auth.presentation.data.request.SignUpRequest
import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import com.team.incube.gsmc.v3.domain.auth.service.OAuthAuthenticationService
import com.team.incube.gsmc.v3.domain.auth.service.SignUpService
import com.team.incube.gsmc.v3.domain.auth.service.TokenRefreshService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
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
    private val oauthAuthenticationService: OAuthAuthenticationService,
    private val tokenRefreshService: TokenRefreshService,
    private val signUpService: SignUpService,
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
    ): AuthTokenResponse = oauthAuthenticationService.execute(code = request.code)

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
    ): AuthTokenResponse = tokenRefreshService.execute(refreshToken = refreshToken)

    @Operation(summary = "회원가입", description = "회원가입합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "회원가입 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 사용자임",
                content = [Content()],
            ),
        ],
    )
    @PostMapping("signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): CommonApiResponse<Nothing> {
        signUpService.execute(name = request.name, studentNumber = request.studentNumber)
        return CommonApiResponse.accepted("OK")
    }
}
