package com.team.incube.gsmc.v3.domain.project.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.member.repository.MemberExposedRepository
import com.team.incube.gsmc.v3.domain.project.entity.ProjectDraftRedisEntity
import com.team.incube.gsmc.v3.domain.project.presentation.data.request.CreateProjectDraftRequest
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectDraftResponse
import com.team.incube.gsmc.v3.domain.project.repository.ProjectDraftRedisRepository
import com.team.incube.gsmc.v3.domain.project.service.CreateMyProjectDraftService
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class CreateMyProjectDraftServiceImpl(
    private val currentMemberProvider: CurrentMemberProvider,
    private val fileExposedRepository: FileExposedRepository,
    private val memberExposedRepository: MemberExposedRepository,
    private val projectDraftRedisRepository: ProjectDraftRedisRepository,
) : CreateMyProjectDraftService {
    override fun execute(request: CreateProjectDraftRequest): GetProjectDraftResponse =
        transaction {
            val memberId = currentMemberProvider.getCurrentMemberId()

            val files =
                if (request.fileIds.isNotEmpty()) {
                    val foundFiles = fileExposedRepository.findAllByIdIn(request.fileIds)
                    if (foundFiles.size != request.fileIds.toSet().size) {
                        throw GsmcException(ErrorCode.FILE_NOT_FOUND)
                    }
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
                if (request.participantIds.isNotEmpty()) {
                    val foundMembers = memberExposedRepository.findAllByIdIn(request.participantIds)
                    if (foundMembers.size != request.participantIds.toSet().size) {
                        throw GsmcException(ErrorCode.MEMBER_NOT_FOUND)
                    }
                    foundMembers
                } else {
                    emptyList()
                }

            val draftEntity =
                ProjectDraftRedisEntity(
                    memberId = memberId,
                    title = request.title,
                    description = request.description,
                    fileIds = files.map { it.id },
                    participantIds = participants.map { it.id },
                )
            projectDraftRedisRepository.save(draftEntity)

            GetProjectDraftResponse(
                title = request.title,
                description = request.description,
                files = files,
                participants = participants,
            )
        }
}
