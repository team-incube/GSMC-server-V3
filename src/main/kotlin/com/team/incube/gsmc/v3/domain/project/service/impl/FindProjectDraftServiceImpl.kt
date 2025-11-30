package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectDraftResponse
import com.team.incube.gsmc.v3.domain.project.repository.redis.ProjectDraftRedisRepository
import com.team.incube.gsmc.v3.domain.project.service.FindProjectDraftService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindProjectDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val projectDraftRedisRepository: ProjectDraftRedisRepository,
    private val fileExposedRepository: FileExposedRepository,
    private val memberExposedRepository: MemberExposedRepository,
) : FindProjectDraftService {
    override fun execute(): GetProjectDraftResponse? {
        val memberId = currentMemberProvider.getCurrentMemberId()
        val draftEntity = projectDraftRedisRepository.findById(memberId).orElse(null) ?: return null

        return transaction {
            val files =
                if (draftEntity.fileIds.isNotEmpty()) {
                    val foundFiles = fileExposedRepository.findAllByIdIn(draftEntity.fileIds)
                    foundFiles.map { file ->
                        FileItem(
                            id = file.id,
                            member = file.member,
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
