package com.team.incube.gsmc.v3.domain.file.service

import com.team.incube.gsmc.v3.domain.file.dto.File
import org.springframework.web.multipart.MultipartFile

interface CreateFileService {
    fun execute(file: MultipartFile): File
}
