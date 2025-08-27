package com.team.incube.gsmc.v3.global.thirdParty.aws.s3.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
data class S3Environment(
    val bucketName: String = "",
)
