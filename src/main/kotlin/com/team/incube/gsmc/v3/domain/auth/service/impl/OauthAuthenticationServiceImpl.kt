package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.entity.RefreshTokenRedisEntity
import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import com.team.incube.gsmc.v3.domain.auth.repository.RefreshTokenRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.OauthAuthenticationService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.JwtProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
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
import java.time.ZoneId

@Service
class OauthAuthenticationServiceImpl(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val jwtProvider: JwtProvider,
    private val memberExposedRepository: MemberExposedRepository,
    private val tokenResponseClient: OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>,
    private val oauth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
) : OauthAuthenticationService {
    override fun execute(code: String): AuthTokenResponse {
        val decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8)

        try {
            val clientRegistration =
                clientRegistrationRepository.findByRegistrationId("google")
                    ?: throw GsmcException(ErrorCode.OAUTH2_AUTHORIZATION_FAILED)

            val authorizationRequest =
                OAuth2AuthorizationRequest
                    .authorizationCode()
                    .clientId(clientRegistration.clientId)
                    .authorizationUri(clientRegistration.providerDetails.authorizationUri)
                    .redirectUri(clientRegistration.redirectUri)
                    .scopes(clientRegistration.scopes)
                    .build()

            val authorizationResponse =
                OAuth2AuthorizationResponse
                    .success(decodedCode)
                    .redirectUri(clientRegistration.redirectUri)
                    .build()

            val authorizationExchange = OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)

            val tokenRequest = OAuth2AuthorizationCodeGrantRequest(clientRegistration, authorizationExchange)

            val tokenResponse = tokenResponseClient.getTokenResponse(tokenRequest)

            val userRequest = OAuth2UserRequest(clientRegistration, tokenResponse.accessToken)
            val oauth2User = oauth2UserService.loadUser(userRequest)

            val email =
                (oauth2User.attributes["email"] as? String)
                    ?: throw GsmcException(ErrorCode.AUTHENTICATION_FAILED)
            val name = (oauth2User.attributes["name"] as? String) ?: ""

            val member =
                transaction {
                    memberExposedRepository.findByEmail(email)
                        ?: memberExposedRepository.save(
                            name = name,
                            email = email,
                            grade = null,
                            classNumber = null,
                            number = null,
                            role = MemberRole.UNAUTHORIZED,
                        )
                }

            val access = jwtProvider.issueAccessToken(member.id, member.role)
            val refresh = jwtProvider.issueRefreshToken(member.id)

            val refreshToken =
                RefreshTokenRedisEntity(
                    token = refresh.token,
                    memberId = member.id,
                    expiration =
                        refresh.expiration
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli(),
                )

            refreshTokenRedisRepository.save(refreshToken)

            return AuthTokenResponse(
                accessToken = access.token,
                accessTokenExpiresAt = access.expiration,
                refreshToken = refresh.token,
                refreshTokenExpiresAt = refresh.expiration,
                role = member.role,
            )
        } catch (e: OAuth2AuthorizationException) {
            throw GsmcException(ErrorCode.OAUTH2_AUTHORIZATION_FAILED)
        } catch (e: Exception) {
            throw GsmcException(ErrorCode.AUTHENTICATION_FAILED)
        }
    }
}
