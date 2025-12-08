package com.team.incube.gsmc.v3.global.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.util.TimeZone

@Configuration
class TimeZoneConfig {
    @PostConstruct
    fun init() {
        System.setProperty("user.timezone", "Asia/Seoul")
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
        logger().info("Default timezone set to Asia/Seoul: {}", LocalDateTime.now())
    }
}
