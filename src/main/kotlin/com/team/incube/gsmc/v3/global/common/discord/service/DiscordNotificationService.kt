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
import org.springframework.stereotype.Service
import java.time.Instant

@Profile("prod")
@Service
class DiscordNotificationService(
    private val discordWebhookClient: DiscordWebhookClient,
) {
    fun sendServerStartNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed =
                    DiscordEmbed(
                        title = "🚀 서버 시작됨",
                        color = EmbedColor.SUCCESS.color,
                        fields =
                            listOf(
                                DiscordField("상태", "GSMC V3 서버 애플리케이션이 시작되었습니다.", false),
                                DiscordField("환경", System.getProperty("spring.profiles.active") ?: "unknown", true),
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
                    color = EmbedColor.WARNING.color,
                    fields =
                        listOf(
                            DiscordField("상태", "GSMC V3 서버 애플리케이션이 종료되었습니다.", false),
                            DiscordField("환경", System.getProperty("spring.profiles.active") ?: "unknown", true),
                        ),
                    timestamp = Instant.now().toString(),
                )

            val payload = DiscordWebhookPayload.embedMessage(embed)
            discordWebhookClient.sendMessage(payload)
        }.onFailure { exception ->
            logger().error("서버 종료 알림 전송 실패", exception)
        }
    }

    fun sendCustomNotification(
        title: String,
        description: String,
        color: EmbedColor = EmbedColor.INFO,
        additionalFields: List<DiscordField> = emptyList(),
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val fields =
                    mutableListOf<DiscordField>().apply {
                        add(DiscordField("내용", description, false))
                        addAll(additionalFields)
                    }

                val embed =
                    DiscordEmbed(
                        title = title,
                        color = color.color,
                        fields = fields,
                        timestamp = Instant.now().toString(),
                    )

                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error("사용자 정의 알림 전송 실패", exception)
            }
        }
    }
}
