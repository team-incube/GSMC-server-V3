package com.team.incube.gsmc.v3.global.scheduler

import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
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
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupOrphanFiles() =
        transaction {
            logger().info("Unused file cleanup started")
            val orphanFiles = fileExposedRepository.findAllUnusedFiles()
            logger().info("Found ${orphanFiles.size} orphan files")
            val (fileUris, fileIds) = orphanFiles.map { it.uri to it.id }.unzip()
            fileExposedRepository.deleteAllByIdIn(fileIds)
            eventPublisher.publishEvent(S3BulkFileDeletionEvent(fileUris))
            logger().info("Orphan file cleanup completed - Total: ${orphanFiles.size}")
        }
}
