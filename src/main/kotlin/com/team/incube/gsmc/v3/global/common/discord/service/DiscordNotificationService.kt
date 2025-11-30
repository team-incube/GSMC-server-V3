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
                        color = EmbedColor.SERVER_START.color,
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
                    color = EmbedColor.SERVER_STOP.color,
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

    fun sendSchedulerStartNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed =
                    DiscordEmbed(
                        title = "🗑️ 미사용 파일 정리 시작",
                        color = EmbedColor.INFO.color,
                        fields =
                            listOf(
                                DiscordField("상태", "사용되지 않는 파일 정리 작업이 시작되었습니다.", false),
                            ),
                        timestamp = Instant.now().toString(),
                    )

                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error("스케줄러 시작 알림 전송 실패", exception)
            }
        }
    }

    fun sendSchedulerEndNotification(deletedFileCount: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed =
                    DiscordEmbed(
                        title = "✅ 미사용 파일 정리 완료",
                        color = EmbedColor.SUCCESS.color,
                        fields =
                            listOf(
                                DiscordField("상태", "사용되지 않는 파일 정리 작업이 완료되었습니다.", false),
                                DiscordField("삭제된 파일 수", "${deletedFileCount}개", true),
                            ),
                        timestamp = Instant.now().toString(),
                    )
                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error("스케줄러 종료 알림 전송 실패", exception)
            }
        }
    }

    fun sendIncompleteScoreSchedulerStartNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed =
                    DiscordEmbed(
                        title = "🧹 미완성 성적 정리 시작",
                        color = EmbedColor.INFO.color,
                        fields =
                            listOf(
                                DiscordField("상태", "미완성 상태의 성적 정리 작업이 시작되었습니다.", false),
                            ),
                        timestamp = Instant.now().toString(),
                    )

                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error("미완성 성적 스케줄러 시작 알림 전송 실패", exception)
            }
        }
    }

    fun sendIncompleteScoreSchedulerEndNotification(deletedScoreCount: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed =
                    DiscordEmbed(
                        title = "✅ 미완성 성적 정리 완료",
                        color = EmbedColor.SUCCESS.color,
                        fields =
                            listOf(
                                DiscordField("상태", "미완성 상태의 성적 정리 작업이 완료되었습니다.", false),
                                DiscordField("삭제된 성적 수", "${deletedScoreCount}개", true),
                            ),
                        timestamp = Instant.now().toString(),
                    )
                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error("미완성 성적 스케줄러 종료 알림 전송 실패", exception)
            }
        }
    }

    fun sendSchedulerFailureNotification(
        schedulerName: String,
        errorMessage: String,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val embed =
                    DiscordEmbed(
                        title = "❌ 스케줄러 작업 실패",
                        color = EmbedColor.ERROR.color,
                        fields =
                            listOf(
                                DiscordField("작업명", schedulerName, true),
                                DiscordField("에러 메시지", errorMessage, false),
                            ),
                        timestamp = Instant.now().toString(),
                    )
                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error("스케줄러 실패 알림 전송 실패", exception)
            }
        }
    }
}
