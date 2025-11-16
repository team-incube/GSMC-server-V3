package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.FileItem
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetMyFilesResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.GetMyFilesService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class GetMyFilesServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : GetMyFilesService {
    override fun execute(): GetMyFilesResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()

            val files = fileExposedRepository.findAllByUserId(member.id)

            GetMyFilesResponse(
                files =
                    files.map { file ->
                        FileItem(
                            fileId = file.fileId,
                            originalName = file.fileOriginalName,
                            storedName = file.fileStoredName,
                            uri = file.fileUri,
                        )
                    },
            )
        }
}