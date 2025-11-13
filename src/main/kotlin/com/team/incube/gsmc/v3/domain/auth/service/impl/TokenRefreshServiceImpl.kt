package com.team.incube.gsmc.v3.domain.auth.service.impl

import com.team.incube.gsmc.v3.domain.auth.entity.RefreshTokenRedisEntity
import com.team.incube.gsmc.v3.domain.auth.presentation.data.response.AuthTokenResponse
import com.team.incube.gsmc.v3.domain.auth.repository.RefreshTokenRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.TokenRefreshService
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.JwtParser
import com.team.incube.gsmc.v3.global.security.jwt.JwtProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class TokenRefreshServiceImpl(
    private val jwtProvider: JwtProvider,
    private val jwtParser: JwtParser,
    private val memberExposedRepository: MemberExposedRepository,
    private val refreshTokenRedisRepository: RefreshTokenRedisRepository,
) : TokenRefreshService {
    override fun execute(refreshToken: String): AuthTokenResponse {
        if (!jwtParser.validateRefreshToken(refreshToken)) {
            throw GsmcException(ErrorCode.REFRESH_TOKEN_INVALID)
        }

        if (!refreshTokenRedisRepository.existsById(refreshToken)) {
            throw GsmcException(ErrorCode.REFRESH_TOKEN_INVALID)
        }

        val memberId = jwtParser.getUserIdFromRefreshToken(refreshToken).toLong()
        val member =
            transaction {
                memberExposedRepository.findById(memberId)
                    ?: throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }

        val newAccess = jwtProvider.issueAccessToken(memberId, member.role)
        val newRefresh = jwtProvider.issueRefreshToken(memberId)

        refreshTokenRedisRepository.deleteById(refreshToken)

        val newRefreshToken =
            RefreshTokenRedisEntity(
                token = newRefresh.token,
                memberId = memberId,
                expiration =
                    newRefresh.expiration
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
            )

        refreshTokenRedisRepository.save(newRefreshToken)

        return AuthTokenResponse(
            accessToken = newAccess.token,
            accessTokenExpiresAt = newAccess.expiration,
            refreshToken = newRefreshToken.token,
            refreshTokenExpiresAt = newRefresh.expiration,
            role = member.role,
        )
    }
}
