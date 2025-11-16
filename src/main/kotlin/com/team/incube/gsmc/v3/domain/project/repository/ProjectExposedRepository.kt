package com.team.incube.gsmc.v3.domain.project.repository

import com.team.incube.gsmc.v3.domain.project.dto.Project
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectExposedRepository {
    fun findProjectById(projectId: Long): Project?

    fun findProjectsByOwnerId(ownerId: Long): List<Project>

    fun findProjectsByParticipantId(participantId: Long): List<Project>

    fun searchProjects(
        title: String?,
        pageable: Pageable,
    ): Page<Project>

    fun saveProject(
        ownerId: Long,
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project

    fun updateProject(
        id: Long,
        ownerId: Long,
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project

    fun deleteProjectById(projectId: Long)
}
