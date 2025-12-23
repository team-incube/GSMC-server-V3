package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.auth.entity.RefreshTokenRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.RefreshTokenRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.impl.OAuthAuthenticationServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.JwtProvider
import com.team.incube.gsmc.v3.global.security.jwt.data.TokenDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.springframework.core.env.Environment
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class OAuthAuthenticationServiceTest :
    BehaviorSpec({

        data class TestContext(
            val clientRegistrationRepository: ClientRegistrationRepository,
            val jwtProvider: JwtProvider,
            val memberExposedRepository: MemberExposedRepository,
            val tokenResponseClient: OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>,
            val oauth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
            val refreshTokenRedisRepository: RefreshTokenRedisRepository,
            val environment: Environment,
            val service: OAuthAuthenticationServiceImpl,
        )

        fun ctx(): TestContext {
            val clientRegistrationRepository = mockk<ClientRegistrationRepository>()
            val jwtProvider = mockk<JwtProvider>()
            val memberExposedRepository = mockk<MemberExposedRepository>()
            val tokenResponseClient = mockk<OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>>()
            val oauth2UserService = mockk<OAuth2UserService<OAuth2UserRequest, OAuth2User>>()
            val refreshTokenRedisRepository = mockk<RefreshTokenRedisRepository>()
            val environment = mockk<Environment>()

            val service =
                OAuthAuthenticationServiceImpl(
                    clientRegistrationRepository = clientRegistrationRepository,
                    jwtProvider = jwtProvider,
                    memberExposedRepository = memberExposedRepository,
                    tokenResponseClient = tokenResponseClient,
                    oauth2UserService = oauth2UserService,
                    refreshTokenRedisRepository = refreshTokenRedisRepository,
                    environment = environment,
                )

            return TestContext(
                clientRegistrationRepository,
                jwtProvider,
                memberExposedRepository,
                tokenResponseClient,
                oauth2UserService,
                refreshTokenRedisRepository,
                environment,
                service,
            )
        }

        val mockTransaction = mockk<JdbcTransaction>(relaxed = true)

        mockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        every {
            org.jetbrains.exposed.v1.jdbc.transactions.transaction(
                db = null,
                statement = any<JdbcTransaction.() -> Any?>(),
            )
        } answers {
            @Suppress("UNCHECKED_CAST")
            val block = it.invocation.args.last() as JdbcTransaction.() -> Any?
            block.invoke(mockTransaction)
        }

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        Given("기존 회원이 Google OAuth 인증 코드로 로그인할 때") {
            val c = ctx()
            val authCode = "valid-google-auth-code"
            val redirectUri = "http://localhost:3000/callback"
            val email = "student@gsm.hs.kr"
            val name = "홍길동"
            val memberId = 1L

            val clientRegistration =
                ClientRegistration
                    .withRegistrationId("google")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .build()

            val accessToken =
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "google-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                )

            val tokenResponse =
                OAuth2AccessTokenResponse
                    .withToken(accessToken.tokenValue)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(3600)
                    .build()

            val oauth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf(
                        "sub" to "google-user-id",
                        "email" to email,
                        "name" to name,
                    ),
                    "sub",
                )

            val existingMember =
                Member(
                    id = memberId,
                    name = name,
                    email = email,
                    grade = 2,
                    classNumber = 3,
                    number = 23,
                    role = MemberRole.STUDENT,
                )

            val fixedTime = LocalDateTime.of(2025, 12, 22, 10, 0)
            val accessTokenDto =
                TokenDto(
                    token = "jwt-access-token",
                    expiration = fixedTime.plusHours(1),
                )
            val refreshTokenDto =
                TokenDto(
                    token = "jwt-refresh-token",
                    expiration = fixedTime.plusDays(7),
                )

            val refreshTokenSlot = slot<RefreshTokenRedisEntity>()

            every { c.environment.activeProfiles } returns arrayOf("dev")
            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration
            every { c.tokenResponseClient.getTokenResponse(any()) } returns tokenResponse
            every { c.oauth2UserService.loadUser(any()) } returns oauth2User
            every { c.memberExposedRepository.findByEmail(email) } returns existingMember
            every { c.jwtProvider.issueAccessToken(memberId, MemberRole.STUDENT) } returns accessTokenDto
            every { c.jwtProvider.issueRefreshToken(memberId) } returns refreshTokenDto
            every { c.refreshTokenRedisRepository.save(capture(refreshTokenSlot)) } answers {
                firstArg<RefreshTokenRedisEntity>()
            }

            When("execute를 호출하면") {
                val result = c.service.execute(authCode, redirectUri)

                Then("AuthTokenResponse가 반환된다") {
                    result shouldNotBe null
                    result.role shouldBe MemberRole.STUDENT
                    result.accessToken shouldBe "jwt-access-token"
                    result.refreshToken shouldBe "jwt-refresh-token"
                    result.accessExpiration shouldBe fixedTime.plusHours(1)
                    result.refreshExpiration shouldBe fixedTime.plusDays(7)
                }

                Then("OAuth2 인증 플로우가 호출된다") {
                    verify(exactly = 1) { c.clientRegistrationRepository.findByRegistrationId("google") }
                    verify(exactly = 1) { c.tokenResponseClient.getTokenResponse(any()) }
                    verify(exactly = 1) { c.oauth2UserService.loadUser(any()) }
                }

                Then("기존 회원을 조회하고 새로 생성하지 않는다") {
                    verify(exactly = 1) { c.memberExposedRepository.findByEmail(email) }
                    verify(exactly = 0) { c.memberExposedRepository.save(any(), any(), any(), any(), any(), any()) }
                }

                Then("JWT 토큰이 발급된다") {
                    verify(exactly = 1) { c.jwtProvider.issueAccessToken(memberId, MemberRole.STUDENT) }
                    verify(exactly = 1) { c.jwtProvider.issueRefreshToken(memberId) }
                }

                Then("RefreshToken이 Redis에 저장된다") {
                    val saved = refreshTokenSlot.captured
                    saved.token shouldBe "jwt-refresh-token"
                    saved.member shouldBe memberId
                    saved.expiration shouldBe
                        refreshTokenDto.expiration
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                }
            }
        }

        Given("신규 회원이 Google OAuth 인증 코드로 로그인할 때") {
            val c = ctx()
            val authCode = "valid-google-auth-code"
            val email = "newstudent@gsm.hs.kr"
            val name = "김신규"

            val clientRegistration =
                ClientRegistration
                    .withRegistrationId("google")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .build()

            val accessToken =
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "google-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                )

            val tokenResponse =
                OAuth2AccessTokenResponse
                    .withToken(accessToken.tokenValue)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(3600)
                    .build()

            val oauth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf(
                        "sub" to "google-new-user-id",
                        "email" to email,
                        "name" to name,
                    ),
                    "sub",
                )

            val newMember =
                Member(
                    id = 2L,
                    name = name,
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )

            val fixedTime = LocalDateTime.of(2025, 12, 22, 10, 0)
            val accessTokenDto =
                TokenDto(
                    token = "jwt-access-token",
                    expiration = fixedTime.plusHours(1),
                )
            val refreshTokenDto =
                TokenDto(
                    token = "jwt-refresh-token",
                    expiration = fixedTime.plusDays(7),
                )

            every { c.environment.activeProfiles } returns arrayOf("dev")
            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration
            every { c.tokenResponseClient.getTokenResponse(any()) } returns tokenResponse
            every { c.oauth2UserService.loadUser(any()) } returns oauth2User
            every { c.memberExposedRepository.findByEmail(email) } returns null
            every {
                c.memberExposedRepository.save(
                    name = name,
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )
            } returns newMember
            every { c.jwtProvider.issueAccessToken(2L, MemberRole.UNAUTHORIZED) } returns accessTokenDto
            every { c.jwtProvider.issueRefreshToken(2L) } returns refreshTokenDto
            every { c.refreshTokenRedisRepository.save(any()) } answers { firstArg() }

            When("execute를 호출하면") {
                val result = c.service.execute(authCode, "http://localhost:3000/callback")

                Then("UNAUTHORIZED 권한으로 회원이 생성된다") {
                    verify(exactly = 1) {
                        c.memberExposedRepository.save(
                            name = name,
                            email = email,
                            grade = null,
                            classNumber = null,
                            number = null,
                            role = MemberRole.UNAUTHORIZED,
                        )
                    }
                }

                Then("AuthTokenResponse가 UNAUTHORIZED 권한으로 반환된다") {
                    result.role shouldBe MemberRole.UNAUTHORIZED
                    result.accessToken shouldBe "jwt-access-token"
                    result.refreshToken shouldBe "jwt-refresh-token"
                }
            }
        }

        Given("프로덕션 환경에서 gsm.hs.kr이 아닌 이메일로 로그인할 때") {
            val c = ctx()
            val authCode = "valid-google-auth-code"
            val email = "invalid@gmail.com"
            val name = "테스트"

            val clientRegistration =
                ClientRegistration
                    .withRegistrationId("google")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .build()

            val accessToken =
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "google-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                )

            val tokenResponse =
                OAuth2AccessTokenResponse
                    .withToken(accessToken.tokenValue)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(3600)
                    .build()

            val oauth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf(
                        "sub" to "google-user-id",
                        "email" to email,
                        "name" to name,
                    ),
                    "sub",
                )

            every { c.environment.activeProfiles } returns arrayOf("prod")
            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration
            every { c.tokenResponseClient.getTokenResponse(any()) } returns tokenResponse
            every { c.oauth2UserService.loadUser(any()) } returns oauth2User

            When("execute를 호출하면") {
                Then("INVALID_EMAIL_DOMAIN 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(authCode, "http://localhost:3000/callback")
                        }
                    ex.errorCode shouldBe ErrorCode.INVALID_EMAIL_DOMAIN
                }
            }
        }

        Given("dev 환경에서 gsm.hs.kr이 아닌 이메일로 로그인할 때") {
            val c = ctx()
            val authCode = "valid-google-auth-code"
            val email = "developer@gmail.com"
            val name = "개발자"

            val clientRegistration =
                ClientRegistration
                    .withRegistrationId("google")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .build()

            val accessToken =
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "google-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                )

            val tokenResponse =
                OAuth2AccessTokenResponse
                    .withToken(accessToken.tokenValue)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(3600)
                    .build()

            val oauth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf(
                        "sub" to "google-user-id",
                        "email" to email,
                        "name" to name,
                    ),
                    "sub",
                )

            val newMember =
                Member(
                    id = 3L,
                    name = name,
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )

            val fixedTime = LocalDateTime.of(2025, 12, 22, 10, 0)
            val accessTokenDto =
                TokenDto(
                    token = "jwt-access-token",
                    expiration = fixedTime.plusHours(1),
                )
            val refreshTokenDto =
                TokenDto(
                    token = "jwt-refresh-token",
                    expiration = fixedTime.plusDays(7),
                )

            every { c.environment.activeProfiles } returns arrayOf("dev")
            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration
            every { c.tokenResponseClient.getTokenResponse(any()) } returns tokenResponse
            every { c.oauth2UserService.loadUser(any()) } returns oauth2User
            every { c.memberExposedRepository.findByEmail(email) } returns null
            every {
                c.memberExposedRepository.save(
                    name = name,
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )
            } returns newMember
            every { c.jwtProvider.issueAccessToken(3L, MemberRole.UNAUTHORIZED) } returns accessTokenDto
            every { c.jwtProvider.issueRefreshToken(3L) } returns refreshTokenDto
            every { c.refreshTokenRedisRepository.save(any()) } answers { firstArg() }

            When("execute를 호출하면") {
                val result = c.service.execute(authCode, "http://localhost:3000/callback")

                Then("dev 환경에서는 이메일 도메인 검증을 건너뛰고 정상 처리된다") {
                    result shouldNotBe null
                    result.role shouldBe MemberRole.UNAUTHORIZED
                }
            }
        }

        Given("Google OAuth2에서 이메일을 제공하지 않을 때") {
            val c = ctx()
            val authCode = "valid-google-auth-code"

            val clientRegistration =
                ClientRegistration
                    .withRegistrationId("google")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .build()

            val accessToken =
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "google-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                )

            val tokenResponse =
                OAuth2AccessTokenResponse
                    .withToken(accessToken.tokenValue)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(3600)
                    .build()

            val oauth2User =
                DefaultOAuth2User(
                    emptyList(),
                    mapOf(
                        "sub" to "google-user-id",
                        "name" to "테스트",
                    ),
                    "sub",
                )

            every { c.environment.activeProfiles } returns arrayOf("dev")
            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration
            every { c.tokenResponseClient.getTokenResponse(any()) } returns tokenResponse
            every { c.oauth2UserService.loadUser(any()) } returns oauth2User

            When("execute를 호출하면") {
                Then("AUTHENTICATION_FAILED 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(authCode, "http://localhost:3000/callback")
                        }
                    ex.errorCode shouldBe ErrorCode.AUTHENTICATION_FAILED
                }
            }
        }

        Given("ClientRegistration을 찾을 수 없을 때") {
            val c = ctx()
            val authCode = "valid-google-auth-code"

            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns null

            When("execute를 호출하면") {
                Then("OAUTH2_AUTHORIZATION_FAILED 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(authCode, "http://localhost:3000/callback")
                        }
                    ex.errorCode shouldBe ErrorCode.OAUTH2_AUTHORIZATION_FAILED
                }
            }
        }

        Given("OAuth2 토큰 발급 중 OAuth2AuthorizationException이 발생할 때") {
            val c = ctx()
            val authCode = "invalid-auth-code"

            val clientRegistration =
                ClientRegistration
                    .withRegistrationId("google")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .build()

            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration
            every { c.tokenResponseClient.getTokenResponse(any()) } throws
                OAuth2AuthorizationException(OAuth2Error("invalid_grant", "Authorization code is invalid", null))

            When("execute를 호출하면") {
                Then("OAUTH2_AUTHORIZATION_FAILED 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(authCode, "http://localhost:3000/callback")
                        }
                    ex.errorCode shouldBe ErrorCode.OAUTH2_AUTHORIZATION_FAILED
                }
            }
        }

        Given("OAuth2 사용자 정보 로드 중 예외가 발생할 때") {
            val c = ctx()
            val authCode = "valid-google-auth-code"

            val clientRegistration =
                ClientRegistration
                    .withRegistrationId("google")
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                    .userNameAttributeName("sub")
                    .build()

            val accessToken =
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "google-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                )

            val tokenResponse =
                OAuth2AccessTokenResponse
                    .withToken(accessToken.tokenValue)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(3600)
                    .build()

            "http://localhost:3000/callback,http://localhost:3001/callback"
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration
            every { c.tokenResponseClient.getTokenResponse(any()) } returns tokenResponse
            every { c.oauth2UserService.loadUser(any()) } throws RuntimeException("User info endpoint error")

            When("execute를 호출하면") {
                Then("AUTHENTICATION_FAILED 예외가 발생한다") {
                    val ex =
                        shouldThrow<GsmcException> {
                            c.service.execute(authCode, "http://localhost:3000/callback")
                        }
                    ex.errorCode shouldBe ErrorCode.AUTHENTICATION_FAILED
                }
            }
        }
    })
