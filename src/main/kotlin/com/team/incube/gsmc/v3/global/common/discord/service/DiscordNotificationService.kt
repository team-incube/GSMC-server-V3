package com.team.incube.gsmc.v3.global.common.discord.service

import com.team.incube.gsmc.v3.global.common.discord.data.DiscordEmbed
import com.team.incube.gsmc.v3.global.common.discord.data.DiscordField
import com.team.incube.gsmc.v3.global.common.discord.data.DiscordWebhookPayload
import com.team.incube.gsmc.v3.global.common.discord.data.EmbedColor
import com.team.incube.gsmc.v3.global.config.logger
import com.team.incube.gsmc.v3.global.thirdparty.feign.client.discord.DiscordWebhookClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private fun getActiveProfile(): String =
        try {
            when {
                environment.activeProfiles.isNotEmpty() -> environment.activeProfiles.joinToString(", ")
                environment.defaultProfiles.isNotEmpty() -> environment.defaultProfiles.joinToString(", ")
                else -> "default"
            }
        } catch (exception: Exception) {
            logger().warn("Failed to retrieve profile information", exception)
            "default"
        }

    private fun sendDiscordNotification(
        embed: DiscordEmbed,
        failureMessage: String,
    ) {
        serviceScope.launch {
            runCatching {
                val payload = DiscordWebhookPayload.embedMessage(embed)
                discordWebhookClient.sendMessage(payload)
            }.onFailure { exception ->
                logger().error(failureMessage, exception)
            }
        }
    }

    fun sendServerStartNotification() {
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
        sendDiscordNotification(embed, "Server start notification failed")
    }

    fun sendServerStopNotification() {
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
        sendDiscordNotification(embed, "Server stop notification failed")
    }

    fun sendSchedulerStartNotification() {
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
        sendDiscordNotification(embed, "Scheduler start notification failed")
    }

    fun sendSchedulerEndNotification(deletedFileCount: Int) {
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
        sendDiscordNotification(embed, "Scheduler end notification failed")
    }

    fun sendIncompleteScoreSchedulerStartNotification() {
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
        sendDiscordNotification(embed, "Incomplete score scheduler start notification failed")
    }

    fun sendIncompleteScoreSchedulerEndNotification(deletedScoreCount: Int) {
        val embed =
            DiscordEmbed(
                title = "✅ 미완성 인증제 점수 정리 완료",
                color = EmbedColor.SUCCESS.color,
                fields =
                    listOf(
                        DiscordField("상태", "미완성 상태의 인증제 점수 정리 작업이 완료되었습니다.", false),
                        DiscordField("삭제된 인증제 점수 수", "${deletedScoreCount}개", true),
                    ),
                timestamp = Instant.now().toString(),
            )
        sendDiscordNotification(embed, "Incomplete score scheduler end notification failed")
    }

    fun sendSchedulerFailureNotification(
        schedulerName: String,
        errorMessage: String,
    ) {
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
        sendDiscordNotification(embed, "Scheduler failure notification failed")
    }
}