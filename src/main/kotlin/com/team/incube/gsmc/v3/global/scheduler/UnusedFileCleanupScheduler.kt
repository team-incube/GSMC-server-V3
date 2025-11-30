package com.team.incube.gsmc.v3.global.scheduler

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceDraftRedisRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.project.repository.ProjectDraftRedisRepository
import com.team.incube.gsmc.v3.global.common.discord.service.DiscordNotificationService
import com.team.incube.gsmc.v3.global.config.logger
import com.team.incube.gsmc.v3.global.event.s3.S3BulkFileDeletionEvent
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UnusedFileCleanupScheduler(
    private val fileExposedRepository: FileExposedRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val discordNotificationService: DiscordNotificationService?,
    private val evidenceDraftRedisRepository: EvidenceDraftRedisRepository,
    private val projectDraftRedisRepository: ProjectDraftRedisRepository,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupOrphanFiles() {
        discordNotificationService?.sendSchedulerStartNotification()
        try {
            transaction {
                logger().info("Unused file cleanup started")

                val protectedFileIds = getProtectedFileIdsFromDrafts()
                logger().info("Found ${protectedFileIds.size} files protected by drafts")

                val orphanFiles = fileExposedRepository.findAllUnusedFiles()
                logger().info("Found ${orphanFiles.size} orphan files")

                val filesToDelete = orphanFiles.filter { it.id !in protectedFileIds }
                logger().info("Files to delete after filtering: ${filesToDelete.size}")

                if (filesToDelete.isEmpty()) {
                    logger().info("No orphan files to clean up")
                    discordNotificationService?.sendSchedulerEndNotification(0)
                    return@transaction
                }

                val (fileUris, fileIds) = filesToDelete.map { it.uri to it.id }.unzip()
                fileExposedRepository.deleteAllByIdIn(fileIds)
                logger().info("Database records deleted - Total: ${fileIds.size}")
                eventPublisher.publishEvent(S3BulkFileDeletionEvent(fileUris))
                logger().info("S3 deletion event published for ${fileUris.size} files")
                discordNotificationService?.sendSchedulerEndNotification(fileIds.size)
            }
        } catch (e: Exception) {
            logger().error("Unused file cleanup failed", e)
            discordNotificationService?.sendSchedulerFailureNotification("미사용 파일 정리", e.message ?: "Unknown error")
        }
    }

    private fun getProtectedFileIdsFromDrafts(): Set<Long> {
        val protectedIds = mutableSetOf<Long>()

        try {
            val evidenceDrafts = evidenceDraftRedisRepository.findAll()
            evidenceDrafts.forEach { draft ->
                protectedIds.addAll(draft.fileIds)
            }
        } catch (e: Exception) {
            logger().error("Failed to get protected file IDs from evidence drafts", e)
        }

        try {
            val projectDrafts = projectDraftRedisRepository.findAll()
            projectDrafts.forEach { draft ->
                protectedIds.addAll(draft.fileIds)
            }
        } catch (e: Exception) {
            logger().error("Failed to get protected file IDs from project drafts", e)
        }

        return protectedIds
    }
}
