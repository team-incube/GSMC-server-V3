package com.team.incube.gsmc.v3.domain.file.repository

import com.team.incube.gsmc.v3.domain.file.dto.File

interface FileExposedRepository {
    fun existsById(fileId: Long): Boolean

    fun existsByIdIn(fileIds: List<Long>): Boolean

    fun saveFile(
        userId: Long,
        originalName: String,
        storedName: String,
        uri: String,
    ): File

    fun findById(fileId: Long): File?

    fun findAllByUserId(userId: Long): List<File>

    fun findUnusedFilesByUserId(userId: Long): List<File>

    fun findAllUnusedFiles(): List<File>

    fun deleteById(fileId: Long)
}
