package com.team.incube.gsmc.v3.global.thirdparty.feign.config

import com.team.incube.gsmc.v3.global.thirdparty.feign.error.FeignErrorDecoder
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableFeignClients(basePackages = ["com.team.incube.gsmc.v3.global.thirdparty.feign.client"])
@Configuration
class FeignConfig {
    @Bean
    fun feignErrorDecoder(): FeignErrorDecoder = FeignErrorDecoder()
}
