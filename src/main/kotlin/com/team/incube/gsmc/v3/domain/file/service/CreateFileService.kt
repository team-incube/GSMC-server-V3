package com.team.incube.gsmc.v3.domain.file.service

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse
import org.springframework.web.multipart.MultipartFile

interface CreateFileService {
    fun execute(file: MultipartFile): CreateFileResponse
}
