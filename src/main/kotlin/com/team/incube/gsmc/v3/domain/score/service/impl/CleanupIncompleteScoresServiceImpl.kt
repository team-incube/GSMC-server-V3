package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.CleanupIncompleteScoresService
import com.team.incube.gsmc.v3.global.config.logger
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CleanupIncompleteScoresServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
) : CleanupIncompleteScoresService {
    override fun execute(): Int =
        transaction {
            logger().info("Incomplete score cleanup started")
            val incompleteScores = scoreExposedRepository.findAllByStatus(ScoreStatus.INCOMPLETE)
            logger().info("Found ${incompleteScores.size} incomplete scores")
            if (incompleteScores.isEmpty()) {
                logger().info("No incomplete scores to clean up")
                return@transaction 0
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
            logger().info("Deleted $deletedCount incomplete scores")
            deletedCount
        }
}
