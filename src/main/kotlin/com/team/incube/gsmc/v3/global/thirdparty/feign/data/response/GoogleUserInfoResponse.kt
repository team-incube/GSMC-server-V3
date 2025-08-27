package com.team.incube.gsmc.v3.global.thirdparty.feign.data.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleUserInfoResponse(
    @param:JsonProperty("id") val id: String,
    @param:JsonProperty("email") val email: String? = null,
    @param:JsonProperty("verified_email") val verifiedEmail: Boolean? = null,
    @param:JsonProperty("name") val name: String? = null,
    @param:JsonProperty("given_name") val givenName: String? = null,
    @param:JsonProperty("family_name") val familyName: String? = null,
    @param:JsonProperty("picture") val picture: String? = null,
    @param:JsonProperty("locale") val locale: String? = null,
)
