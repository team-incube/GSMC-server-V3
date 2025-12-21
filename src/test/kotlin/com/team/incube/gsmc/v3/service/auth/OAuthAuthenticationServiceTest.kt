package com.team.incube.gsmc.v3.service.auth

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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime

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
            val refreshTokenRedisRepository = mockk<RefreshTokenRedisRepository>(relaxed = true)
            val environment = mockk<Environment>()

            val service =
                OAuthAuthenticationServiceImpl(
                    clientRegistrationRepository,
                    jwtProvider,
                    memberExposedRepository,
                    tokenResponseClient,
                    oauth2UserService,
                    refreshTokenRedisRepository,
                    environment,
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

        fun clientRegistration(): ClientRegistration =
            ClientRegistration
                .withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("auth-uri")
                .tokenUri("token-uri")
                .redirectUri("redirect-uri")
                .userInfoUri("user-info-uri")
                .userNameAttributeName("email")
                .scope("email", "profile")
                .clientName("Google")
                .build()

        Given("정상적인 OAuth 로그인 요청일 때") {
            val c = ctx()
            val code = "auth-code"
            val email = "test@gsm.hs.kr"

            val oauth2User =
                mockk<OAuth2User> {
                    every { attributes } returns mapOf(
                        "email" to email,
                        "name" to "홍길동",
                    )
                }

            val member =
                Member(
                    id = 1L,
                    name = "홍길동",
                    email = email,
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.UNAUTHORIZED,
                )

            val access =
                TokenDto(
                    token = "access",
                    expiration = LocalDateTime.now(),
                )

            val refresh =
                TokenDto(
                    token = "refresh",
                    expiration = LocalDateTime.now(),
                )

            every { c.environment.activeProfiles } returns arrayOf("dev")
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration()
            every { c.tokenResponseClient.getTokenResponse(any()) } returns mockk(relaxed = true)
            every { c.oauth2UserService.loadUser(any()) } returns oauth2User
            every { c.memberExposedRepository.findByEmail(email) } returns member
            every { c.jwtProvider.issueAccessToken(member.id, member.role) } returns access
            every { c.jwtProvider.issueRefreshToken(member.id) } returns refresh

            When("execute를 호출하면") {
                val result = c.service.execute(code)

                Then("토큰 정보가 정상적으로 반환된다") {
                    result.accessToken shouldBe "access"
                    result.refreshToken shouldBe "refresh"
                    result.role shouldBe MemberRole.UNAUTHORIZED
                }

                Then("RefreshToken이 저장된다") {
                    verify(exactly = 1) { c.refreshTokenRedisRepository.save(any()) }
                }
            }
        }

        Given("ClientRegistration이 없을 때") {
            val c = ctx()
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns null

            When("execute를 호출하면") {
                Then("OAUTH2_AUTHORIZATION_FAILED 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> {
                        c.service.execute("code")
                    }
                    ex.errorCode shouldBe ErrorCode.OAUTH2_AUTHORIZATION_FAILED
                }
            }
        }

        Given("운영 환경에서 이메일 도메인이 유효하지 않을 때") {
            val c = ctx()

            val oauth2User =
                mockk<OAuth2User> {
                    every { attributes } returns mapOf(
                        "email" to "test@gmail.com",
                        "name" to "홍길동",
                    )
                }

            every { c.environment.activeProfiles } returns arrayOf("prod")
            every { c.clientRegistrationRepository.findByRegistrationId("google") } returns clientRegistration()
            every { c.tokenResponseClient.getTokenResponse(any()) } returns mockk(relaxed = true)
            every { c.oauth2UserService.loadUser(any()) } returns oauth2User

            When("execute를 호출하면") {
                Then("INVALID_EMAIL_DOMAIN 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> {
                        c.service.execute("code")
                    }
                    ex.errorCode shouldBe ErrorCode.INVALID_EMAIL_DOMAIN
                }
            }
        }
    })
