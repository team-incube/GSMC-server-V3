package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.FileItem
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetCurrentFilesResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.FindCurrentFilesService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindCurrentFilesServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindCurrentFilesService {
    override fun execute(): GetCurrentFilesResponse =
        transaction {
            val member = currentMemberProvider.getCurrentUser()

            val files = fileExposedRepository.findAllByUserId(member.id)

            GetCurrentFilesResponse(
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