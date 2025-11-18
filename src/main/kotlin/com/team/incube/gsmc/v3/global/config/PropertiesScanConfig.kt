package com.team.incube.gsmc.v3.global.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "com.team.incube.gsmc.v3.global.security.data",
        "com.team.incube.gsmc.v3.global.thirdparty.aws.s3.data",
        "com.team.incube.gsmc.v3.global.security.jwt.data",
    ],
)
class PropertiesScanConfig
