package com.team.incube.gsmc.v3.global.security.oauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * Spring Security OAuth2 Client 공식 SDK 설정
 * - DefaultAuthorizationCodeTokenResponseClient: Authorization Code → Access Token 교환
 * - DefaultOAuth2UserService: Access Token → 사용자 정보 조회
 */
@Configuration
class OAuth2ClientConfig(
    private val clientRegistrationRepository: ClientRegistrationRepository,
) {
    /**
     * Authorization Code를 Access Token으로 교환하는 공식 SDK 클라이언트
     * application/x-www-form-urlencoded 형식을 자동으로 처리
     */
    @Bean
    fun oauth2AccessTokenResponseClient(): OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> =
        DefaultAuthorizationCodeTokenResponseClient()

    /**
     * Access Token으로 사용자 정보를 가져오는 공식 SDK 서비스
     */
    @Bean
    fun oauth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> = DefaultOAuth2UserService()
}
