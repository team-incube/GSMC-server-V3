package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import com.team.incube.gsmc.v3.domain.auth.service.OauthAuthenticationService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.security.jwt.JwtProvider
import com.team.incube.gsmc.v3.global.security.oauth.data.OauthEnvironment
import com.team.incube.gsmc.v3.global.thirdparty.feign.client.oauth.GoogleOAuth2Client
import com.team.incube.gsmc.v3.global.thirdparty.feign.client.oauth.GoogleUserInfoClient
import com.team.incube.gsmc.v3.global.thirdparty.feign.data.response.GoogleUserInfoResponse
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Service
class OauthAuthenticationServiceImpl(
    private val googleOAuth2Client: GoogleOAuth2Client,
    private val googleUserInfoClient: GoogleUserInfoClient,
    private val jwtProvider: JwtProvider,
    private val oauthEnv: OauthEnvironment,
    private val memberExposedRepository: MemberExposedRepository,
) : OauthAuthenticationService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun execute(code: String): AuthTokenResponse {

        val form =
            mapOf(
                "grant_type" to "authorization_code",
                "client_id" to oauthEnv.clientId,
                "client_secret" to oauthEnv.clientSecret,
                "code" to URLDecoder.decode(code, StandardCharsets.UTF_8),
                "redirect_uri" to oauthEnv.redirectUri,
            )
        val tokenResponse = googleOAuth2Client.exchangeCodeForToken(form)

        val userInfo: GoogleUserInfoResponse =
            try {
                googleUserInfoClient.getUserInfo("Bearer ${tokenResponse.accessToken}")
            } catch (e: Exception) {
                throw e
            }

        val member =
            transaction {
                memberExposedRepository.findByEmail(userInfo.email!!)
                    ?: memberExposedRepository.save(
                        name = userInfo.name ?: "",
                        email = userInfo.email,
                        grade = null,
                        classNumber = null,
                        number = null,
                        role = MemberRole.UNAUTHORIZED,
                    )
            }

        val access = jwtProvider.issueAccessToken(member.id, member.role)
        val refresh = jwtProvider.issueRefreshToken(member.id)

        return AuthTokenResponse(
            accessToken = access.token,
            accessTokenExpiresAt = access.expiration,
            refreshToken = refresh.token,
            refreshTokenExpiresAt = refresh.expiration,
            role = member.role,
        )
    }
}
