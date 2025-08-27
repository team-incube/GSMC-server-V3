package com.team.incube.gsmc.v3.global.thirdparty.feign.config

import com.team.incube.gsmc.v3.global.thirdparty.feign.error.FeignErrorDecoder
import feign.Contract
import feign.codec.Decoder
import feign.codec.Encoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableFeignClients(basePackages = ["com.team.incube.gsmc.v3.global.thirdparty.feign.client"])
@Configuration
class FeignConfig {
    @Bean
    fun feignErrorDecoder(): FeignErrorDecoder = FeignErrorDecoder()

    @Bean
    fun encoder(): Encoder = JacksonEncoder()

    @Bean
    fun decoder(): Decoder = JacksonDecoder()

    @Bean
    fun contract(): Contract = SpringMvcContract()
}
