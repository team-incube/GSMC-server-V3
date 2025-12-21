package com.team.incube.gsmc.v3.domain.auth.presentation

import com.team.incube.gsmc.v3.domain.auth.presentation.data.request.CreateTeacherSignUpRequest
import com.team.incube.gsmc.v3.domain.auth.presentation.data.request.OAuthCodeRequest
import com.team.incube.gsmc.v3.domain.auth.presentation.data.request.SignUpRequest
import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.GetTeacherSignUpRequestResponse
import com.team.incube.gsmc.v3.domain.auth.service.ApproveTeacherSignUpRequestService
import com.team.incube.gsmc.v3.domain.auth.service.CreateTeacherSignUpRequestService
import com.team.incube.gsmc.v3.domain.auth.service.FindMyTeacherSignUpRequestService
import com.team.incube.gsmc.v3.domain.auth.service.FindTeacherSignUpRequestsService
import com.team.incube.gsmc.v3.domain.auth.service.OAuthAuthenticationService
import com.team.incube.gsmc.v3.domain.auth.service.RejectTeacherSignUpRequestService
import com.team.incube.gsmc.v3.domain.auth.service.SignUpService
import com.team.incube.gsmc.v3.domain.auth.service.TokenRefreshService
import com.team.incube.gsmc.v3.global.common.cookie.CookieUtil
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@Tag(name = "Auth API", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v3/auth")
class AuthController(
    private val oauthAuthenticationService: OAuthAuthenticationService,
    private val tokenRefreshService: TokenRefreshService,
    private val signUpService: SignUpService,
    private val createTeacherSignUpRequestService: CreateTeacherSignUpRequestService,
    private val findTeacherSignUpRequestsService: FindTeacherSignUpRequestsService,
    private val findMyTeacherSignUpRequestService: FindMyTeacherSignUpRequestService,
    private val approveTeacherSignUpRequestService: ApproveTeacherSignUpRequestService,
    private val rejectTeacherSignUpRequestService: RejectTeacherSignUpRequestService,
    private val cookieUtil: CookieUtil,
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
        response: HttpServletResponse,
    ): AuthTokenResponse {
        val tokenPair = oauthAuthenticationService.execute(code = request.code)

        val accessTokenMaxAge = Duration.between(java.time.LocalDateTime.now(), tokenPair.accessTokenExpiresAt)
        val refreshTokenMaxAge = Duration.between(java.time.LocalDateTime.now(), tokenPair.refreshTokenExpiresAt)

        val accessCookie = cookieUtil.createAccessTokenCookie(tokenPair.accessToken, accessTokenMaxAge)
        val refreshCookie = cookieUtil.createRefreshTokenCookie(tokenPair.refreshToken, refreshTokenMaxAge)

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

        return AuthTokenResponse(role = tokenPair.role)
    }

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
        response: HttpServletResponse,
    ): AuthTokenResponse {
        val tokenPair = tokenRefreshService.execute(refreshToken = refreshToken)

        val accessTokenMaxAge = Duration.between(java.time.LocalDateTime.now(), tokenPair.accessTokenExpiresAt)
        val refreshTokenMaxAge = Duration.between(java.time.LocalDateTime.now(), tokenPair.refreshTokenExpiresAt)

        val accessCookie = cookieUtil.createAccessTokenCookie(tokenPair.accessToken, accessTokenMaxAge)
        val refreshCookie = cookieUtil.createRefreshTokenCookie(tokenPair.refreshToken, refreshTokenMaxAge)

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

        return AuthTokenResponse(role = tokenPair.role)
    }

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

    @Operation(summary = "교직원 회원가입 요청", description = "교직원 권한 회원가입을 요청합니다 (UNAUTHORIZED 권한만 가능)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "회원가입 요청 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (담임선생님의 경우 학년/반 필수, 권한 검증 실패)",
                content = [Content()],
            ),
        ],
    )
    @PostMapping("/teacher-signup")
    fun createTeacherSignUpRequest(
        @Valid @RequestBody request: CreateTeacherSignUpRequest,
    ): CommonApiResponse<Nothing> {
        createTeacherSignUpRequestService.execute(
            name = request.name,
            requestedRole = request.requestedRole,
            grade = request.grade,
            classNumber = request.classNumber,
        )
        return CommonApiResponse.accepted("OK")
    }

    @Operation(summary = "교직원 회원가입 요청 목록 조회", description = "대기 중인 교직원 회원가입 요청 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
        ],
    )
    @GetMapping("/teacher-signup/requests")
    fun getTeacherSignUpRequests(): List<GetTeacherSignUpRequestResponse> = findTeacherSignUpRequestsService.execute()

    @Operation(summary = "현재 인증된 사용자의 교직원 회원가입 요청 정보 조회", description = "현재 로그인한 사용자의 교직원 회원가입 요청 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "회원가입 요청을 찾을 수 없음",
                content = [Content()],
            ),
        ],
    )
    @GetMapping("/teacher-signup/my-request")
    fun getMyTeacherSignUpRequest(): GetTeacherSignUpRequestResponse = findMyTeacherSignUpRequestService.execute()

    @Operation(summary = "교직원 회원가입 요청 승인", description = "교직원 회원가입 요청을 승인합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "승인 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "요청을 찾을 수 없음",
                content = [Content()],
            ),
        ],
    )
    @PatchMapping("/teacher-signup/{memberId}/approve")
    fun approveTeacherSignUpRequest(
        @PathVariable memberId: Long,
    ): CommonApiResponse<Nothing> {
        approveTeacherSignUpRequestService.execute(memberId)
        return CommonApiResponse.accepted("OK")
    }

    @Operation(summary = "교직원 회원가입 요청 거부", description = "교직원 회원가입 요청을 거부하고 해당 회원을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "거부 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "요청을 찾을 수 없음",
                content = [Content()],
            ),
        ],
    )
    @PatchMapping("/teacher-signup/{memberId}/reject")
    fun rejectTeacherSignUpRequest(
        @PathVariable memberId: Long,
    ): CommonApiResponse<Nothing> {
        rejectTeacherSignUpRequestService.execute(memberId)
        return CommonApiResponse.accepted("OK")
    }
}
