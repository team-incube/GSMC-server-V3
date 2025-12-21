package com.team.incube.gsmc.v3.service.auth

import com.team.incube.gsmc.v3.domain.auth.repository.RefreshTokenRedisRepository
import com.team.incube.gsmc.v3.domain.auth.service.impl.TokenRefreshServiceImpl
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.JwtParser
import com.team.incube.gsmc.v3.global.security.jwt.JwtProvider
import com.team.incube.gsmc.v3.global.security.jwt.data.TokenDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import java.time.LocalDateTime

class TokenRefreshServiceTest :
    BehaviorSpec({

        data class TestContext(
            val jwtProvider: JwtProvider,
            val jwtParser: JwtParser,
            val memberExposedRepository: MemberExposedRepository,
            val refreshTokenRedisRepository: RefreshTokenRedisRepository,
            val service: TokenRefreshServiceImpl,
        )

        fun ctx(): TestContext {
            val jwtProvider = mockk<JwtProvider>()
            val jwtParser = mockk<JwtParser>()
            val memberExposedRepository = mockk<MemberExposedRepository>()
            val refreshTokenRedisRepository = mockk<RefreshTokenRedisRepository>(relaxed = true)

            val service =
                TokenRefreshServiceImpl(
                    jwtProvider = jwtProvider,
                    jwtParser = jwtParser,
                    memberExposedRepository = memberExposedRepository,
                    refreshTokenRedisRepository = refreshTokenRedisRepository,
                )

            return TestContext(
                jwtProvider,
                jwtParser,
                memberExposedRepository,
                refreshTokenRedisRepository,
                service,
            )
        }

        val mockTransaction = mockk<JdbcTransaction>(relaxed = true)

        mockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        every {
            org.jetbrains.exposed.v1.jdbc.transactions.transaction(
                db = null,
                statement = any<JdbcTransaction.() -> Any?>(),
            )
        } answers {
            @Suppress("UNCHECKED_CAST")
            val block = it.invocation.args.last() as JdbcTransaction.() -> Any?
            block.invoke(mockTransaction)
        }

        afterSpec {
            unmockkStatic("org.jetbrains.exposed.v1.jdbc.transactions.TransactionsKt")
        }

        Given("유효한 RefreshToken으로 재발급을 요청할 때") {
            val c = ctx()
            val refreshToken = "valid-refresh-token"
            val memberId = 1L

            val member =
                Member(
                    id = memberId,
                    name = "홍길동",
                    email = "test@gsm.hs.kr",
                    grade = null,
                    classNumber = null,
                    number = null,
                    role = MemberRole.STUDENT,
                )

            val newAccess =
                TokenDto(
                    token = "new-access-token",
                    expiration = LocalDateTime.now().plusSeconds(3600),
                )

            val newRefresh =
                TokenDto(
                    token = "new-refresh-token",
                    expiration = LocalDateTime.now().plusSeconds(7200),
                )

            every { c.jwtParser.validateRefreshToken(refreshToken) } returns true
            every { c.refreshTokenRedisRepository.existsById(refreshToken) } returns true
            every { c.jwtParser.getUserIdFromRefreshToken(refreshToken) } returns memberId.toString()
            every { c.memberExposedRepository.findById(memberId) } returns member
            every { c.jwtProvider.issueAccessToken(memberId, member.role) } returns newAccess
            every { c.jwtProvider.issueRefreshToken(memberId) } returns newRefresh
            every { c.refreshTokenRedisRepository.deleteById(refreshToken) } returns Unit

            When("execute를 호출하면") {
                val result = c.service.execute(refreshToken)

                Then("새로운 AccessToken과 RefreshToken이 반환된다") {
                    result.accessToken shouldBe "new-access-token"
                    result.refreshToken shouldBe "new-refresh-token"
                    result.role shouldBe MemberRole.STUDENT
                }

                Then("기존 RefreshToken은 삭제되고 새 토큰이 저장된다") {
                    verify(exactly = 1) { c.refreshTokenRedisRepository.deleteById(refreshToken) }
                    verify(exactly = 1) { c.refreshTokenRedisRepository.save(any()) }
                }
            }
        }

        Given("RefreshToken이 유효하지 않을 때") {
            val c = ctx()
            val refreshToken = "invalid-refresh-token"

            every { c.jwtParser.validateRefreshToken(refreshToken) } returns false

            When("execute를 호출하면") {
                Then("REFRESH_TOKEN_INVALID 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> {
                        c.service.execute(refreshToken)
                    }
                    ex.errorCode shouldBe ErrorCode.REFRESH_TOKEN_INVALID
                }
            }
        }

        Given("Redis에 RefreshToken이 존재하지 않을 때") {
            val c = ctx()
            val refreshToken = "not-exists-token"

            every { c.jwtParser.validateRefreshToken(refreshToken) } returns true
            every { c.refreshTokenRedisRepository.existsById(refreshToken) } returns false

            When("execute를 호출하면") {
                Then("REFRESH_TOKEN_INVALID 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> {
                        c.service.execute(refreshToken)
                    }
                    ex.errorCode shouldBe ErrorCode.REFRESH_TOKEN_INVALID
                }
            }
        }

        Given("토큰에 담긴 회원이 DB에 존재하지 않을 때") {
            val c = ctx()
            val refreshToken = "valid-but-no-member"
            val memberId = 99L

            every { c.jwtParser.validateRefreshToken(refreshToken) } returns true
            every { c.refreshTokenRedisRepository.existsById(refreshToken) } returns true
            every { c.jwtParser.getUserIdFromRefreshToken(refreshToken) } returns memberId.toString()
            every { c.memberExposedRepository.findById(memberId) } returns null

            When("execute를 호출하면") {
                Then("MEMBER_NOT_FOUND 예외가 발생한다") {
                    val ex = shouldThrow<GsmcException> {
                        c.service.execute(refreshToken)
                    }
                    ex.errorCode shouldBe ErrorCode.MEMBER_NOT_FOUND
                }
            }
        }
    })

