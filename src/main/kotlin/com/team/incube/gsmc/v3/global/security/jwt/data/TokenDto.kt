package com.team.incube.gsmc.v3.global.security.jwt.data

import java.time.LocalDateTime

data class TokenDto(
    val token: String,
    val expiration: LocalDateTime,
)
