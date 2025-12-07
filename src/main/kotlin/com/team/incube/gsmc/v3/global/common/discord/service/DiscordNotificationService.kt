package com.team.incube.gsmc.v3.global.common.discord.service

import com.team.incube.gsmc.v3.global.common.discord.data.DiscordEmbed
import com.team.incube.gsmc.v3.global.common.discord.data.DiscordField
import com.team.incube.gsmc.v3.global.common.discord.data.DiscordWebhookPayload
import com.team.incube.gsmc.v3.global.common.discord.data.EmbedColor
import com.team.incube.gsmc.v3.global.config.logger
import com.team.incube.gsmc.v3.global.thirdparty.feign.client.discord.DiscordWebhookClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.time.Instant

@Profile("prod")
@Service
class DiscordNotificationService(
    private val discordWebhookClient: DiscordWebhookClient,
    private val environment: Environment,
) {
    private fun getActiveProfile(): String =
        try {
            when {
                environment.activeProfiles.isNotEmpty() -> environment.activeProfiles.joinToString(", ")
                environment.defaultProfiles.isNotEmpty() -> environment.defaultProfiles.joinToString(", ")
                else -> "default"
            }
        } catch (exception: Exception) {
            logger().warn("프로파일 정보를 가져오는데 실패했습니다", exception)
            "default"
        }

    fun sendServerStartNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed =
                    DiscordEmbed(
                        title = "🚀 서버 시작됨",
                        color = EmbedColor.SERVER_START.color,
                        fields =
                            listOf(
                                DiscordField("상태", "GSMC V3 서버 애플리케이션이 시작되었습니다.", false),
                                DiscordField("환경", getActiveProfile(), true),
                            ),
                        timestamp = Instant.now().toString(),
                    )

                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error("서버 시작 알림 전송 실패", exception)
            }
        }
    }

    fun sendServerStopNotification() {
        runCatching {
            val embed =
                DiscordEmbed(
                    title = "🛑 서버 종료됨",
                    color = EmbedColor.SERVER_STOP.color,
                    fields =
                        listOf(
                            DiscordField("상태", "GSMC V3 서버 애플리케이션이 종료되었습니다.", false),
                            DiscordField("환경", getActiveProfile(), true),
                        ),
                    timestamp = Instant.now().toString(),
                )

            val payload = DiscordWebhookPayload.embedMessage(embed)
            discordWebhookClient.sendMessage(payload)
        }.onFailure { exception ->
            logger().error("서버 종료 알림 전송 실패", exception)
        }
    }
}
