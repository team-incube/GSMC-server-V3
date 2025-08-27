package com.team.incube.gsmc.v3.global.thirdparty.feign.client.oauth

import com.team.incube.gsmc.v3.global.thirdparty.feign.data.response.GoogleUserInfoResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "google-userinfo-client",
    url = "https://www.googleapis.com",
)
interface GoogleUserInfoClient {
    @GetMapping("/oauth2/v2/userinfo")
    fun getUserInfo(
        @RequestHeader("Authorization") authorization: String?,
    ): GoogleUserInfoResponse
}
