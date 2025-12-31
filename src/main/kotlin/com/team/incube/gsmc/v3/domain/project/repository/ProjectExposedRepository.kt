package com.team.incube.gsmc.v3.domain.project.repository

import com.team.incube.gsmc.v3.domain.project.dto.Project
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProjectExposedRepository {
    fun findById(projectId: Long): Project?

    fun findAllByOwnerId(ownerId: Long): List<Project>

    fun findAllByParticipantId(participantId: Long): List<Project>

    fun searchProjects(
        title: String?,
        pageable: Pageable,
    ): Page<Project>

    fun save(
        ownerId: Long,
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project

    fun update(
        id: Long,
        ownerId: Long,
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project

    fun existsProjectParticipantByProjectIdAndMemberId(
        projectId: Long,
        memberId: Long,
    ): Boolean

    fun findTitleById(projectId: Long): String?

    fun findProjectTitleAndValidateParticipant(
        projectId: Long,
        memberId: Long,
    ): String?

    fun deleteById(projectId: Long)

    fun findScoreIdsByProjectId(projectId: Long): List<Long>

    fun linkProjectAndScore(
        projectId: Long,
        scoreId: Long,
    )
}
