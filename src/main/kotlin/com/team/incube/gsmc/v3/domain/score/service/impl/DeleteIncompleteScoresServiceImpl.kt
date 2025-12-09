package com.team.incube.gsmc.v3.domain.score.service.impl

import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.domain.score.service.DeleteIncompleteScoresService
import com.team.incube.gsmc.v3.global.config.logger
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteIncompleteScoresServiceImpl(
    private val scoreExposedRepository: ScoreExposedRepository,
) : DeleteIncompleteScoresService {
    override fun execute(): Int =
        transaction {
            logger().info("Incomplete score cleanup started")
            val incompleteScores = scoreExposedRepository.findAllByStatus(ScoreStatus.INCOMPLETE)
            logger().info("Found ${incompleteScores.size} incomplete scores")
            val scoreIdsToDelete = incompleteScores.mapNotNull { it.id }
            if (scoreIdsToDelete.isEmpty()) {
                logger().info("No incomplete scores with valid IDs to delete")
                return@transaction 0
            }
            scoreExposedRepository.deleteAllByIdIn(scoreIdsToDelete)
            logger().info("Deleted ${scoreIdsToDelete.size} incomplete scores")
            scoreIdsToDelete.size
        }
}
