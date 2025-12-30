package com.team.incube.gsmc.v3.domain.file.service

import com.team.incube.gsmc.v3.domain.file.presentation.data.request.CreatePresignedUploadUrlRequest
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreatePresignedUploadUrlResponse

interface CreatePresignedUploadUrlService {
    fun execute(request: CreatePresignedUploadUrlRequest): CreatePresignedUploadUrlResponse
}
