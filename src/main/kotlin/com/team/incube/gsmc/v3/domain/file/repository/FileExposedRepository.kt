package com.team.incube.gsmc.v3.domain.file.repository

import com.team.incube.gsmc.v3.domain.file.dto.File

interface FileExposedRepository {
    fun existsById(fileId: Long): Boolean

    fun existsByIdIn(fileIds: List<Long>): Boolean

    fun save(
        userId: Long,
        originalName: String,
        storedName: String,
        uri: String,
    ): File

    fun findById(fileId: Long): File?

    fun findAllByIdIn(fileIds: List<Long>): List<File>

    fun findAllByMemberId(memberId: Long): List<File>

    fun findUnusedFilesByMemberId(memberId: Long): List<File>

    fun findAllUnusedFiles(): List<File>

    fun deleteById(fileId: Long)

    fun deleteAllByIdIn(fileIds: List<Long>)
}
