package com.team.incube.gsmc.v3.global.thirdparty.feign.client.oauth

import com.team.incube.gsmc.v3.global.thirdparty.feign.data.response.GoogleTokenResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "google-oauth2-client",
    url = "https://oauth2.googleapis.com",
)
interface GoogleOAuth2Client {
    @PostMapping(value = ["/token"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun exchangeCodeForToken(
        @RequestParam("grant_type") grantType: String = "authorization_code",
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam("code") code: String,
        @RequestParam("redirect_uri") redirectUri: String,
    ): GoogleTokenResponse
}
