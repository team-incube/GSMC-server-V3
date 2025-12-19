package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.category.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.developer.service.DeleteMemberByEmailService
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.project.entity.ProjectFileExposedEntity
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.config.logger
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class DeleteMemberByEmailServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val alertExposedRepository: AlertExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val s3DeleteService: S3DeleteService,
) : DeleteMemberByEmailService {
    override fun execute(email: String) {
        transaction {
            val member =
                memberExposedRepository.findByEmail(email)
                    ?: run {
                        logger().info("Member withdrawal failed: member not found. email={}", email)
                        throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
                    }

            alertExposedRepository.deleteAllByMemberId(member.id)

            val memberFiles = fileExposedRepository.findAllByUserId(member.id)
            val memberFileIds = memberFiles.map { it.id }

            memberFiles.forEach { file ->
                s3DeleteService.execute(file.uri)
            }

            if (memberFileIds.isNotEmpty()) {
                ProjectFileExposedEntity.deleteWhere {
                    ProjectFileExposedEntity.file inList memberFileIds
                }
                fileExposedRepository.deleteAllByIdIn(memberFileIds)
            }

            val scores = scoreExposedRepository.findAllByMemberId(member.id)
            if (scores.isNotEmpty()) {
                val scoreIds = scores.mapNotNull { it.id }

                if (scoreIds.isNotEmpty()) {
                    alertExposedRepository.deleteAllByScoreIdIn(scoreIds)
                }

                scores.forEach { score ->
                    val sourceId = score.sourceId ?: return@forEach

                    when (score.categoryType.evidenceType) {
                        EvidenceType.EVIDENCE -> {
                            evidenceExposedRepository.deleteById(sourceId)
                        }

                        EvidenceType.FILE,
                        EvidenceType.UNREQUIRED,
                        -> {
                            Unit
                        }
                    }
                }

                scoreExposedRepository.deleteAllByIdIn(scoreIds)
            }

            val deleted = memberExposedRepository.deleteMemberByEmail(email)
            if (deleted == 0) {
                logger().warn("Member withdrawal failed unexpectedly after it was found. email={}", email)
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }
        }

        logger().info("Member withdrawn successfully. email={}", email)
    }
}
