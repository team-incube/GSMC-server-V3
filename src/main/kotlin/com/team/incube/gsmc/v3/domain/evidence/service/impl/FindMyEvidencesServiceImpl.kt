package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetEvidenceResponse
import com.team.incube.gsmc.v3.domain.evidence.presentation.data.response.GetMyEvidencesResponse
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.FindMyEvidencesService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyEvidencesServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindMyEvidencesService {
    override fun execute(): GetMyEvidencesResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()

            val evidences = evidenceExposedRepository.findAllByMemberId(memberId = member.id)

            GetMyEvidencesResponse(
                evidences =
                    evidences.map { evidence ->
                        GetEvidenceResponse(
                            id = evidence.id,
                            title = evidence.title,
                            content = evidence.content,
                            createdAt = evidence.createdAt,
                            updatedAt = evidence.updatedAt,
                            files = evidence.files,
                        )
                    },
            )
        }
}
