package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceDraftRedisRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.CleanupUnusedFilesService
import com.team.incube.gsmc.v3.domain.project.repository.ProjectDraftRedisRepository
import com.team.incube.gsmc.v3.global.config.logger
import com.team.incube.gsmc.v3.global.event.s3.S3BulkFileDeletionEvent
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class CleanupUnusedFilesServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val evidenceDraftRedisRepository: EvidenceDraftRedisRepository,
    private val projectDraftRedisRepository: ProjectDraftRedisRepository,
) : CleanupUnusedFilesService {
    override fun execute(): Int =
        transaction {
            logger().info("Unused file cleanup started")
            val protectedFileIds = getProtectedFileIdsFromDrafts()
            logger().info("Found ${protectedFileIds.size} files protected by drafts")
            val orphanFiles = fileExposedRepository.findAllUnusedFiles()
            logger().info("Found ${orphanFiles.size} orphan files")
            val filesToDelete = orphanFiles.filter { it.id !in protectedFileIds }
            logger().info("Deleting ${filesToDelete.size} files")
            if (filesToDelete.isEmpty()) {
                logger().info("No orphan files to clean up")
                return@transaction 0
            }
            val (fileUris, fileIds) = filesToDelete.map { it.uri to it.id }.unzip()
            fileExposedRepository.deleteAllByIdIn(fileIds)
            logger().info("Deleted ${fileIds.size} file records from the database")
            eventPublisher.publishEvent(S3BulkFileDeletionEvent(fileUris))
            logger().info("Published S3 deletion event for ${fileUris.size} files")
            fileIds.size
        }

    private fun getProtectedFileIdsFromDrafts(): Set<Long> {
        val evidenceFileIds =
            try {
                evidenceDraftRedisRepository.findAll().flatMap { it.fileIds }
            } catch (e: Exception) {
                logger().error("Failed to get protected file IDs from evidence drafts", e)
                emptyList()
            }

        val projectFileIds =
            try {
                projectDraftRedisRepository.findAll().flatMap { it.fileIds }
            } catch (e: Exception) {
                logger().error("Failed to get protected file IDs from project drafts", e)
                emptyList()
            }

        return (evidenceFileIds + projectFileIds).toSet()
    }
}
