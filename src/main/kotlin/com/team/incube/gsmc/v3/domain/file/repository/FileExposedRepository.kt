package com.team.incube.gsmc.v3.domain.file.repository

import com.team.incube.gsmc.v3.domain.file.dto.File

interface FileExposedRepository {
    fun existsByIdIn(fileIds: List<Long>): Boolean
    fun saveFile(originalName: String, storedName: String, uri: String): File
}
