package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectDraftResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectDraftRedisRepository
import com.team.incube.gsmc.v3.domain.project.service.FindMyProjectDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyProjectDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val projectDraftRedisRepository: ProjectDraftRedisRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val memberExposedRepository: MemberExposedRepository,
) : FindMyProjectDraftService {
    override fun execute(): GetProjectDraftResponse? {
        val memberId = currentMemberProvider.getCurrentMemberId()
        val draftEntity = projectDraftRedisRepository.findById(memberId).orElse(null) ?: return null

        return transaction {
            val files =
                if (draftEntity.fileIds.isNotEmpty()) {
                    val foundFiles = fileExposedRepository.findAllByIdIn(draftEntity.fileIds)
                    foundFiles.map { file ->
                        GetFileResponse(
                            id = file.id,
                            memberId = file.member,
                            originalName = file.originalName,
                            storeName = file.storeName,
                            uri = file.uri,
                        )
                    }
                } else {
                    emptyList()
                }

            val participants =
                if (draftEntity.participantIds.isNotEmpty()) {
                    memberExposedRepository.findAllByIdIn(draftEntity.participantIds)
                } else {
                    emptyList()
                }

            GetProjectDraftResponse(
                title = draftEntity.title,
                description = draftEntity.description,
                files = files,
                participants = participants,
            )
        }
    }
}
