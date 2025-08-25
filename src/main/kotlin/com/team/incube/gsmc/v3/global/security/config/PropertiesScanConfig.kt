package com.team.incube.gsmc.v3.global.security.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan(
    basePackages = [
        "com.team.incube.gsmc.v3.global.security.data"]
)
class PropertiesScanConfig {
}