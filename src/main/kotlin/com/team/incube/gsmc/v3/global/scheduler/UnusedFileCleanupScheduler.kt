package com.team.incube.gsmc.v3.global.scheduler

import com.team.incube.gsmc.v3.domain.file.service.CleanupUnusedFilesService
import com.team.incube.gsmc.v3.global.common.discord.service.DiscordNotificationService
import com.team.incube.gsmc.v3.global.config.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UnusedFileCleanupScheduler(
    private val cleanupUnusedFilesService: CleanupUnusedFilesService,
    private val discordNotificationService: DiscordNotificationService?,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupOrphanFiles() {
        discordNotificationService?.sendSchedulerStartNotification()
        try {
            val deletedCount = cleanupUnusedFilesService.execute()
            discordNotificationService?.sendSchedulerEndNotification(deletedCount)
        } catch (e: Exception) {
            logger().error("Unused file cleanup failed", e)
            discordNotificationService?.sendSchedulerFailureNotification("미사용 파일 정리", e.message ?: "Unknown error")
        }
    }
}
