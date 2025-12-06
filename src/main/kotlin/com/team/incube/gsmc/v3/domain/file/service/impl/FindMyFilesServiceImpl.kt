package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetMyFilesResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.FindMyFilesService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyFilesServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindMyFilesService {
    override fun execute(): GetMyFilesResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()

            val files = fileExposedRepository.findAllByUserId(member.id)

            GetMyFilesResponse(
                files =
                    files.map { file ->
                        GetFileResponse(
                            id = file.id,
                            originalName = file.originalName,
                            storeName = file.storeName,
                            uri = file.uri,
                            memberId = file.member,
                        )
                    },
            )
        }
}
