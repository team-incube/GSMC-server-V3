package com.team.incube.gsmc.v3.domain.file.service

import com.team.incube.gsmc.v3.domain.file.presentation.data.request.ConfirmFileUploadRequest
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse

interface ConfirmFileUploadService {
    fun execute(request: ConfirmFileUploadRequest): CreateFileResponse
}
