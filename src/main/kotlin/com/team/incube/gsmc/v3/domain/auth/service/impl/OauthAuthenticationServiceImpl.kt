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
//        log.info("[OAuth] Received authorization code: {}", code.take(15) + "…")
//
//        val decodedCode = try {
//            URLDecoder.decode(code, StandardCharsets.UTF_8)
//        } catch (e: Exception) {
//            log.warn("[OAuth] Failed to decode code, using raw value", e)
//            code
//        }
//
//        val form = LinkedMultiValueMap<String, String>().apply {
//            add("grant_type", "authorization_code")
//            add("client_id", oauthEnv.clientId)
//            add("client_secret", oauthEnv.clientSecret)
//            add("code", decodedCode)
//            add("redirect_uri", oauthEnv.redirectUri)
//        }
//
//        log.info("[OAuth] Requesting Google token: redirectUri={}, clientId={}", oauthEnv.redirectUri, oauthEnv.clientId)
//
//        val tokenResponse: GoogleTokenResponse = try {
//            googleOAuth2Client.exchangeCodeForToken(
//                contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
//                form = form
//            )
//        } catch (e: Exception) {
//            log.error("[OAuth] Google token request failed: {}", e.message, e)
//            throw e
//        }

        val form =
            mapOf(
                "grant_type" to "authorization_code",
                "client_id" to oauthEnv.clientId,
                "client_secret" to oauthEnv.clientSecret,
                "code" to URLDecoder.decode(code, StandardCharsets.UTF_8),
                "redirect_uri" to oauthEnv.redirectUri,
            )
        val tokenResponse = googleOAuth2Client.exchangeCodeForToken(form)

        log.info(
            "[OAuth] Google token response received: access_token(length={}), expires_in={}",
            tokenResponse.accessToken?.length ?: 0,
            tokenResponse.expiresIn,
        )

        val userInfo: GoogleUserInfoResponse =
            try {
                googleUserInfoClient.getUserInfo("Bearer ${tokenResponse.accessToken}")
            } catch (e: Exception) {
                log.error("[OAuth] Failed to get user info from Google: {}", e.message, e)
                throw e
            }

        log.info("[OAuth] Google user info: name={}, email={}", userInfo.name, userInfo.email)

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

        log.info("[OAuth] Member resolved: id={}, role={}", member.id, member.role)

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
    }
}
