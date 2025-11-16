package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.FileItem
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetMyFilesResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.GetMyUnusedFilesService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class GetMyUnusedFilesServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : GetMyUnusedFilesService {
    override fun execute(): GetMyFilesResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()

            val unusedFiles = fileExposedRepository.findUnusedFilesByUserId(member.id)

            GetMyFilesResponse(
                files =
                    unusedFiles.map { file ->
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