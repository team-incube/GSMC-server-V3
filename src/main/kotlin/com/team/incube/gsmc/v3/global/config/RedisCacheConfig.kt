package com.team.incube.gsmc.v3.global.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.Cache
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.SerializationException

@Configuration
@EnableCaching
class RedisCacheConfig : CachingConfigurer {
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val typeValidator =
            BasicPolymorphicTypeValidator
                .builder()
                .allowIfSubType("com.team.incube.gsmc.v3")
                .allowIfSubType("java.util")
                .allowIfSubType("java.lang")
                .build()

        val objectMapper =
            ObjectMapper().apply {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                activateDefaultTyping(
                    typeValidator,
                    ObjectMapper.DefaultTyping.EVERYTHING,
                    JsonTypeInfo.As.PROPERTY,
                )
            }

        val jsonSerializer =
            object : RedisSerializer<Any> {
                override fun serialize(value: Any?): ByteArray =
                    if (value == null) {
                        ByteArray(0)
                    } else {
                        try {
                            objectMapper.writeValueAsBytes(value)
                        } catch (e: Exception) {
                            throw SerializationException("Failed to serialize object", e)
                        }
                    }

                override fun deserialize(bytes: ByteArray?): Any? =
                    if (bytes == null || bytes.isEmpty()) {
                        null
                    } else {
                        try {
                            objectMapper.readValue(bytes, Any::class.java)
                        } catch (e: Exception) {
                            throw SerializationException("Failed to deserialize object", e)
                        }
                    }
            }

        val redisCacheConfiguration =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()),
                ).serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer),
                )

        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .build()
    }

    override fun errorHandler(): CacheErrorHandler =
        object : CacheErrorHandler {
            override fun handleCacheGetError(
                exception: RuntimeException,
                cache: Cache,
                key: Any,
            ) {
                logger().warn("Cache get error for key: $key, evicting cache entry", exception)
                cache.evict(key)
            }

            override fun handleCachePutError(
                exception: RuntimeException,
                cache: Cache,
                key: Any,
                value: Any?,
            ) {
                logger().error("Cache put error for key: $key", exception)
            }

            override fun handleCacheEvictError(
                exception: RuntimeException,
                cache: Cache,
                key: Any,
            ) {
                logger().error("Cache evict error for key: $key", exception)
            }

            override fun handleCacheClearError(
                exception: RuntimeException,
                cache: Cache,
            ) {
                logger().error("Cache clear error", exception)
            }
        }
}
