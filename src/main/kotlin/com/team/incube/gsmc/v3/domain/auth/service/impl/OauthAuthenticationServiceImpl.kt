package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import com.team.incube.gsmc.v3.domain.auth.service.OauthAuthenticationService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.security.jwt.JwtProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Spring Security OAuth2 Client SDK를 활용한 OAuth 인증 서비스
 * - OAuth2AccessTokenResponseClient: Authorization Code → Access Token (공식 SDK)
 * - OAuth2UserService: Access Token → User Info (공식 SDK)
 */
@Service
class OauthAuthenticationServiceImpl(
    private val tokenResponseClient: OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>,
    private val oauth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val jwtProvider: JwtProvider,
    private val memberExposedRepository: MemberExposedRepository,
) : OauthAuthenticationService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(code: String): AuthTokenResponse {
        // URL decode the authorization code (프론트엔드에서 JSON으로 전송 시 인코딩된 상태로 옴)
        val decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8)
        log.info("[OAuth] Received authorization code: {}", code.take(15) + "…")
        log.info("[OAuth] Decoded authorization code: {}", decodedCode.take(15) + "…")

        try {
            // Step 1: Get Google ClientRegistration from application.yaml
            val clientRegistration = clientRegistrationRepository.findByRegistrationId("google")
            log.info(
                "[OAuth] ClientRegistration loaded: clientId={}, redirectUri={}, scopes={}",
                clientRegistration.clientId,
                clientRegistration.redirectUri,
                clientRegistration.scopes,
            )

            // Step 2: Build OAuth2 Authorization Exchange (SDK way)
            val authorizationExchange = buildAuthorizationExchange(decodedCode, clientRegistration)

            // Step 3: Exchange code for token using official SDK
            val tokenRequest =
                OAuth2AuthorizationCodeGrantRequest(
                    clientRegistration,
                    authorizationExchange,
                )
            val tokenResponse = tokenResponseClient.getTokenResponse(tokenRequest)

            log.info(
                "[OAuth] Google token response received: access_token(length={}), expires_in={}",
                tokenResponse.accessToken.tokenValue.length,
                tokenResponse.accessToken.expiresAt?.epochSecond,
            )

            // Step 4: Get user info using official SDK
            val userRequest = OAuth2UserRequest(clientRegistration, tokenResponse.accessToken)
            val oauth2User = oauth2UserService.loadUser(userRequest)

            val email = oauth2User.getAttribute<String>("email")
            val name = oauth2User.getAttribute<String>("name")

            log.info("[OAuth] Google user info: name={}, email={}", name, email)

            // Step 5: Find or create member
            val member =
                transaction {
                    memberExposedRepository.findByEmail(email!!)
                        ?: memberExposedRepository.save(
                            name = name ?: "",
                            email = email,
                            grade = null,
                            classNumber = null,
                            number = null,
                            role = MemberRole.UNAUTHORIZED,
                        )
                }

            log.info("[OAuth] Member resolved: id={}, role={}", member.id, member.role)

            // Step 6: Issue JWT tokens
            val access = jwtProvider.issueAccessToken(member.id, member.role)
            val refresh = jwtProvider.issueRefreshToken(member.id)

            log.info("[OAuth] Issued JWT tokens for user {} (exp: {})", member.email, access.expiration)

            return AuthTokenResponse(
                accessToken = access.token,
                accessTokenExpiresAt = access.expiration,
                refreshToken = refresh.token,
                refreshTokenExpiresAt = refresh.expiration,
                role = member.role,
            ).also {
                log.info("[OAuth] Authentication complete for {}", member.email)
            }
        } catch (e: OAuth2AuthorizationException) {
            log.error("[OAuth] Authorization failed: {}", e.error.description, e)
            throw e
        } catch (e: Exception) {
            log.error("[OAuth] Unexpected error during OAuth flow: {}", e.message, e)
            throw e
        }
    }

    /**
     * Build OAuth2AuthorizationExchange from authorization code
     * This simulates the OAuth2 flow that Spring Security normally handles automatically
     */
    private fun buildAuthorizationExchange(
        code: String,
        clientRegistration: ClientRegistration,
    ): OAuth2AuthorizationExchange {
        // Build authorization request (what was sent to Google)
        val authorizationRequest =
            OAuth2AuthorizationRequest
                .authorizationCode()
                .clientId(clientRegistration.clientId)
                .authorizationUri(clientRegistration.providerDetails.authorizationUri)
                .redirectUri(clientRegistration.redirectUri)
                .scopes(clientRegistration.scopes)
                .state("state") // Normally this should be validated
                .build()

        // Build authorization response (what we got back from Google)
        val authorizationResponse =
            OAuth2AuthorizationResponse
                .success(code)
                .redirectUri(clientRegistration.redirectUri)
                .state("state")
                .build()

        return OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
    }
}
