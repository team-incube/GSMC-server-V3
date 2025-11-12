package com.team.incube.gsmc.v3.global.thirdparty.feign.client.oauth

import com.team.incube.gsmc.v3.global.thirdparty.feign.data.response.GoogleTokenResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "google-oauth2-client",
    url = "https://oauth2.googleapis.com",
)
interface GoogleOAuth2Client {
    @PostMapping(value = ["/token"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun exchangeCodeForToken(
        @RequestBody form: Map<String, String>,
    ): GoogleTokenResponse
}
