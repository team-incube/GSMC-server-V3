package com.team.incube.gsmc.v3.domain.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import java.util.concurrent.TimeUnit

@RedisHash("refresh_token")
class RefreshTokenRedisEntity(
    @Id
    val token: String,
    @Indexed
    val member: Long,
    @TimeToLive(unit = TimeUnit.SECONDS)
    val expiration: Long,
)
