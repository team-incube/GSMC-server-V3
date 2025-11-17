package com.team.incube.gsmc.v3.domain.file.service

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetMyFilesResponse

interface FindMyUnusedFilesService {
    fun execute(): GetMyFilesResponse
}