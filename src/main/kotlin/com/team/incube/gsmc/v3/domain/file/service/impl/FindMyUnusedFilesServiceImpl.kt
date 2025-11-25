package com.team.incube.gsmc.v3.domain.file.service.impl

import com.team.incube.gsmc.v3.domain.file.presentation.data.dto.FileItem
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetMyFilesResponse
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.file.service.FindMyUnusedFilesService
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class FindMyUnusedFilesServiceImpl(
    private val fileExposedRepository: FileExposedRepository,
    private val currentMemberProvider: CurrentMemberProvider,
) : FindMyUnusedFilesService {
    override fun execute(): GetMyFilesResponse =
        transaction {
            val member = currentMemberProvider.getCurrentMember()

            val unusedFiles = fileExposedRepository.findUnusedFilesByUserId(member.id)

            GetMyFilesResponse(
                files =
                    unusedFiles.map { file ->
                        FileItem(
                            fileId = file.fileId,
                            fileOriginalName = file.fileOriginalName,
                            fileStoreName = file.fileStoreName,
                            fileUri = file.fileUri,
                            memberId = file.memberId,
                        )
                    },
            )
        }
}
