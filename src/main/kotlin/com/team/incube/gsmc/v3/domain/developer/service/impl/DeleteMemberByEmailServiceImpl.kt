package com.team.incube.gsmc.v3.domain.developer.service.impl

import com.team.incube.gsmc.v3.domain.category.constant.EvidenceType
import com.team.incube.gsmc.v3.domain.developer.service.DeleteMemberByEmailService
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteMemberByEmailServiceImpl(
    private val memberExposedRepository: MemberExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val s3DeleteService: S3DeleteService,
) : DeleteMemberByEmailService {
    private val log = LoggerFactory.getLogger(DeleteMemberByEmailServiceImpl::class.java)

    override fun execute(email: String) {
        transaction {
            val member =
                memberExposedRepository.findByEmail(email)
                    ?: run {
                        log.info("Member withdrawal failed: member not found. email={}", email)
                        throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
                    }

            val scores = scoreExposedRepository.findAllByMemberId(member.id)

            if (scores.isNotEmpty()) {
                val scoreIds = mutableListOf<Long>()

                scores.forEach { score ->
                    val scoreId = score.id ?: return@forEach
                    scoreIds += scoreId

                    score.sourceId?.let { sourceId ->
                        when (score.categoryType.evidenceType) {
                            EvidenceType.EVIDENCE -> {
                                val evidence = evidenceExposedRepository.findById(sourceId)
                                evidence?.files?.forEach { file ->
                                    s3DeleteService.execute(file.uri)
                                    fileExposedRepository.deleteById(file.id)
                                }
                                evidenceExposedRepository.deleteById(sourceId)
                            }

                            EvidenceType.FILE -> {
                                val file = fileExposedRepository.findById(sourceId)
                                file?.let {
                                    s3DeleteService.execute(it.uri)
                                    fileExposedRepository.deleteById(it.id)
                                }
                            }

                            EvidenceType.UNREQUIRED -> {
                            }
                        }
                    }
                }

                scoreExposedRepository.deleteByIdIn(scoreIds)
            }

            val deleted = memberExposedRepository.deleteMemberByEmail(email)

            if (deleted == 0) {
                log.warn("Member withdrawal failed unexpectedly after it was found. email={}", email)
                throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
            }
        }

        log.info("Member withdrawn successfully. email={}", email)
    }
}
