package com.team.incube.gsmc.v3.global.scheduler

import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.global.config.logger
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UnusedFileCleanupScheduler(
    private val fileExposedRepository: FileExposedRepository,
    private val s3DeleteService: S3DeleteService,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupOrphanFiles() =
        transaction {
            logger().info("Unused file cleanup started")

            var successCount = 0
            var failCount = 0

            transaction {
                val orphanFiles = fileExposedRepository.findAllUnusedFiles()
                logger().info("Found ${orphanFiles.size} orphan files")
                orphanFiles.forEach { file ->
                    try {
                        s3DeleteService.execute(file.uri)
                        fileExposedRepository.deleteById(file.id)
                        successCount++
                    } catch (e: Exception) {
                        failCount++
                        logger().error("File deletion failed: id=${file.id}, uri=${file.uri} - ${e.message}")
                    }
                }
            }
            logger().info("Orphan file cleanup completed - Success: $successCount, Failures: $failCount")
        }
}
