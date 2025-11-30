package com.team.incube.gsmc.v3.global.scheduler

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.discord.service.DiscordNotificationService
import com.team.incube.gsmc.v3.global.config.logger
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class IncompleteScoreCleanupScheduler(
    private val scoreExposedRepository: ScoreExposedRepository,
    private val discordNotificationService: DiscordNotificationService?,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupIncompleteScores() {
        discordNotificationService?.sendIncompleteScoreSchedulerStartNotification()
        try {
            transaction {
                logger().info("Incomplete score cleanup started")
                val incompleteScores = scoreExposedRepository.findAllByStatus(ScoreStatus.INCOMPLETE)
                logger().info("Found ${incompleteScores.size} incomplete scores")
                if (incompleteScores.isEmpty()) {
                    logger().info("No incomplete scores to clean up")
                    discordNotificationService?.sendIncompleteScoreSchedulerEndNotification(0)
                    return@transaction
                }
                var deletedCount = 0
                incompleteScores.forEach { score ->
                    try {
                        scoreExposedRepository.deleteById(score.id!!)
                        deletedCount++
                    } catch (e: Exception) {
                        logger().error("Score deletion failed: id=${score.id} - ${e.message}")
                    }
                }
                logger().info("Incomplete score cleanup completed - Deleted: $deletedCount")
                discordNotificationService?.sendIncompleteScoreSchedulerEndNotification(deletedCount)
            }
        } catch (e: Exception) {
            logger().error("Incomplete score cleanup failed", e)
            discordNotificationService?.sendSchedulerFailureNotification("미완성 성적 정리", e.message ?: "Unknown error")
        }
    }
}
