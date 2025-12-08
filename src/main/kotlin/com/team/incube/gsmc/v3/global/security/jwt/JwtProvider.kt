package com.team.incube.gsmc.v3.global.security.jwt

import com.team.incube.gsmc.v3.domain.auth.entity.RefreshTokenRedisEntity
import com.team.incube.gsmc.v3.domain.auth.repository.RefreshTokenRedisRepository
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.security.jwt.data.JwtEnvironment
import com.team.incube.gsmc.v3.global.security.jwt.data.TokenDto
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Component
class JwtProvider(
    private val jwtEnvironment: JwtEnvironment,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
) {
    private lateinit var accessTokenKey: javax.crypto.SecretKey
    private lateinit var refreshTokenKey: javax.crypto.SecretKey

    @PostConstruct
    fun init() {
        accessTokenKey = Keys.hmacShaKeyFor(jwtEnvironment.accessToken.secret.toByteArray())
        refreshTokenKey = Keys.hmacShaKeyFor(jwtEnvironment.refreshToken.secret.toByteArray())
    }

    fun issueAccessToken(
        memberId: Long,
        role: MemberRole,
    ): TokenDto =
        TokenDto(
            token =
                Jwts
                    .builder()
                    .subject(memberId.toString())
                    .issuer(jwtEnvironment.issuer)
                    .issuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                    .expiration(
                        Date.from(
                            LocalDateTime
                                .now()
                                .plusMinutes(
                                    jwtEnvironment.accessToken.expiration,
                                ).atZone(ZoneId.systemDefault())
                                .toInstant(),
                        ),
                    ).claim("role", role.name)
                    .signWith(accessTokenKey, SignatureAlgorithm.HS256)
                    .compact(),
            expiration = LocalDateTime.now().plusMinutes(jwtEnvironment.accessToken.expiration),
        )

    fun issueRefreshToken(memberId: Long): TokenDto {
        val token =
            TokenDto(
                token =
                    Jwts
                        .builder()
                        .subject(memberId.toString())
                        .issuer(jwtEnvironment.issuer)
                        .issuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                        .expiration(
                            Date.from(
                                LocalDateTime
                                    .now()
                                    .plusMinutes(
                                        jwtEnvironment.refreshToken.expiration,
                                    ).atZone(ZoneId.systemDefault())
                                    .toInstant(),
                            ),
                        ).signWith(refreshTokenKey, SignatureAlgorithm.HS512)
                        .compact(),
                expiration = LocalDateTime.now().plusMinutes(jwtEnvironment.refreshToken.expiration),
            )

        refreshTokenRedisRepository.save(
            RefreshTokenRedisEntity(
                token = token.token,
                member = memberId,
                expiration =
                    token.expiration
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
            ),
        )

        return token
    }
}
