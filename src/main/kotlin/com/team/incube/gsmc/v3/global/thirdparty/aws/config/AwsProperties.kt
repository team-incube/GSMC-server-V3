package com.team.incube.gsmc.v3.global.thirdparty.aws.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.cloud.aws")
data class AwsProperties(
    val region: Region,
    val credentials: Credentials,
) {
    data class Region(
        val static: String,
    )

    data class Credentials(
        val accessKey: String,
        val secretKey: String,
    )
}
