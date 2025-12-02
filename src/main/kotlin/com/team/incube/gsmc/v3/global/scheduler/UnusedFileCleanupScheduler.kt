package com.team.incube.gsmc.v3.global.scheduler

import com.team.incube.gsmc.v3.domain.file.service.DeleteUnusedFilesService
import com.team.incube.gsmc.v3.global.common.discord.service.DiscordNotificationService
import com.team.incube.gsmc.v3.global.config.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UnusedFileCleanupScheduler(
    private val deleteUnusedFilesService: DeleteUnusedFilesService,
    private val discordNotificationService: DiscordNotificationService?,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupOrphanFiles() {
        discordNotificationService?.sendSchedulerStartNotification()
        try {
            val deletedCount = deleteUnusedFilesService.execute()
            discordNotificationService?.sendSchedulerEndNotification(deletedCount)
        } catch (e: Exception) {
            logger().error("Unused file cleanup failed", e)
            discordNotificationService?.sendSchedulerFailureNotification("미사용 파일 정리", e.message ?: "Unknown error")
        }
    }
}
