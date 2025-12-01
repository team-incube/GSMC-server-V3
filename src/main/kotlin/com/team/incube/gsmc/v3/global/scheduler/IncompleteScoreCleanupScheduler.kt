package com.team.incube.gsmc.v3.global.scheduler

import com.team.incube.gsmc.v3.domain.score.service.CleanupIncompleteScoresService
import com.team.incube.gsmc.v3.global.common.discord.service.DiscordNotificationService
import com.team.incube.gsmc.v3.global.config.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class IncompleteScoreCleanupScheduler(
    private val cleanupIncompleteScoresService: CleanupIncompleteScoresService,
    private val discordNotificationService: DiscordNotificationService?,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupIncompleteScores() {
        discordNotificationService?.sendIncompleteScoreSchedulerStartNotification()
        try {
            val deletedCount = cleanupIncompleteScoresService.execute()
            discordNotificationService?.sendIncompleteScoreSchedulerEndNotification(deletedCount)
        } catch (e: Exception) {
            logger().error("Incomplete score cleanup failed", e)
            discordNotificationService?.sendSchedulerFailureNotification("미완성 성적 정리", e.message ?: "Unknown error")
        }
    }
}
