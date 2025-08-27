package com.team.incube.gsmc.v3.global.thirdparty.feign.data.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleTokenResponse(
    @param:JsonProperty("access_token") val accessToken: String,
    @param:JsonProperty("token_type") val tokenType: String,
    @param:JsonProperty("expires_in") val expiresIn: Int,
    @param:JsonProperty("refresh_token") val refreshToken: String? = null,
    @param:JsonProperty("scope") val scope: String? = null,
    @param:JsonProperty("id_token") val idToken: String? = null,
)
