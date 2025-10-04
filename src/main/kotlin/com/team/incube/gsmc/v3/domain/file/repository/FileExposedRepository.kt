package com.team.incube.gsmc.v3.domain.file.repository

interface FileExposedRepository {
    fun existsByIdIn(fileIds: List<Long>): Boolean
}
