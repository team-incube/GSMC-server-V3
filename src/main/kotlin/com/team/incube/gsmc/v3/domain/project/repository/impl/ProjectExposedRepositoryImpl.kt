package com.team.incube.gsmc.v3.domain.project.repository.impl

import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.entity.ProjectExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectFileExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectParticipantExposedEntity
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ProjectExposedRepositoryImpl : ProjectExposedRepository {
    override fun findProjectById(projectId: Long): Project? {
        val projectRow =
            ProjectExposedEntity
                .selectAll()
                .where { ProjectExposedEntity.id eq projectId }
                .singleOrNull()
                ?: return null

        val files = getProjectFiles(projectId)
        val participants = getProjectParticipants(projectId)

        return Project(
            id = projectRow[ProjectExposedEntity.id],
            ownerId = projectRow[ProjectExposedEntity.ownerId],
            title = projectRow[ProjectExposedEntity.title],
            description = projectRow[ProjectExposedEntity.description],
            files = files,
            participants = participants,
        )
    }

    override fun findProjectsByOwnerId(ownerId: Long): List<Project> {
        val projectRows =
            ProjectExposedEntity
                .selectAll()
                .where { ProjectExposedEntity.ownerId eq ownerId }

        return projectRows.map { projectRow ->
            val projectId = projectRow[ProjectExposedEntity.id]
            val files = getProjectFiles(projectId)
            val participants = getProjectParticipants(projectId)

            Project(
                id = projectId,
                ownerId = projectRow[ProjectExposedEntity.ownerId],
                title = projectRow[ProjectExposedEntity.title],
                description = projectRow[ProjectExposedEntity.description],
                files = files,
                participants = participants,
            )
        }
    }

    override fun findProjectsByParticipantId(participantId: Long): List<Project> {
        val projectIds =
            ProjectParticipantExposedEntity
                .selectAll()
                .where { ProjectParticipantExposedEntity.memberId eq participantId }
                .map { it[ProjectParticipantExposedEntity.projectId] }

        if (projectIds.isEmpty()) return emptyList()

        val projectRows =
            ProjectExposedEntity
                .selectAll()
                .where { ProjectExposedEntity.id inList projectIds }

        return projectRows.map { projectRow ->
            val projectId = projectRow[ProjectExposedEntity.id]
            val files = getProjectFiles(projectId)
            val participants = getProjectParticipants(projectId)

            Project(
                id = projectId,
                ownerId = projectRow[ProjectExposedEntity.ownerId],
                title = projectRow[ProjectExposedEntity.title],
                description = projectRow[ProjectExposedEntity.description],
                files = files,
                participants = participants,
            )
        }
    }

    override fun searchProjects(
        title: String?,
        pageable: Pageable,
    ): Page<Project> {
        var query = ProjectExposedEntity.selectAll()

        if (!title.isNullOrBlank()) {
            query = query.where { ProjectExposedEntity.title like "%$title%" }
        }

        val total = query.count()

        val projectRows =
            query
                .orderBy(ProjectExposedEntity.id to SortOrder.DESC)
                .limit(pageable.pageSize)
                .offset(pageable.offset)

        val projects =
            projectRows.map { projectRow ->
                val projectId = projectRow[ProjectExposedEntity.id]
                val files = getProjectFiles(projectId)
                val participants = getProjectParticipants(projectId)

                Project(
                    id = projectId,
                    ownerId = projectRow[ProjectExposedEntity.ownerId],
                    title = projectRow[ProjectExposedEntity.title],
                    description = projectRow[ProjectExposedEntity.description],
                    files = files,
                    participants = participants,
                )
            }

        return PageImpl(projects, pageable, total)
    }

    override fun saveProject(
        ownerId: Long,
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project {
        val projectId =
            ProjectExposedEntity.insert {
                it[this.ownerId] = ownerId
                it[this.title] = title
                it[this.description] = description
            } get ProjectExposedEntity.id

        if (fileIds.isNotEmpty()) {
            ProjectFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[ProjectFileExposedEntity.projectId] = projectId
                this[ProjectFileExposedEntity.fileId] = fileId
            }
        }

        // 대표자를 참여자에 자동으로 포함
        val allParticipantIds = (participantIds + ownerId).distinct()

        if (allParticipantIds.isNotEmpty()) {
            ProjectParticipantExposedEntity.batchInsert(allParticipantIds) { memberId ->
                this[ProjectParticipantExposedEntity.projectId] = projectId
                this[ProjectParticipantExposedEntity.memberId] = memberId
            }
        }

        val files = getProjectFiles(projectId)
        val participants = getProjectParticipants(projectId)

        return Project(
            id = projectId,
            ownerId = ownerId,
            title = title,
            description = description,
            files = files,
            participants = participants,
        )
    }

    override fun updateProject(
        id: Long,
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project {
        val ownerId =
            ProjectExposedEntity
                .selectAll()
                .where { ProjectExposedEntity.id eq id }
                .single()[ProjectExposedEntity.ownerId]

        ProjectExposedEntity.update({ ProjectExposedEntity.id eq id }) {
            it[this.title] = title
            it[this.description] = description
        }

        ProjectFileExposedEntity.deleteWhere { ProjectFileExposedEntity.projectId eq id }
        ProjectParticipantExposedEntity.deleteWhere { ProjectParticipantExposedEntity.projectId eq id }

        if (fileIds.isNotEmpty()) {
            ProjectFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[ProjectFileExposedEntity.projectId] = id
                this[ProjectFileExposedEntity.fileId] = fileId
            }
        }

        val allParticipantIds = (participantIds + ownerId).distinct()

        if (allParticipantIds.isNotEmpty()) {
            ProjectParticipantExposedEntity.batchInsert(allParticipantIds) { memberId ->
                this[ProjectParticipantExposedEntity.projectId] = id
                this[ProjectParticipantExposedEntity.memberId] = memberId
            }
        }

        val files = getProjectFiles(id)
        val participants = getProjectParticipants(id)

        return Project(
            id = id,
            ownerId = ownerId,
            title = title,
            description = description,
            files = files,
            participants = participants,
        )
    }

    override fun deleteProjectById(projectId: Long) {
        ProjectFileExposedEntity.deleteWhere { ProjectFileExposedEntity.projectId eq projectId }
        ProjectParticipantExposedEntity.deleteWhere { ProjectParticipantExposedEntity.projectId eq projectId }
        ProjectExposedEntity.deleteWhere { ProjectExposedEntity.id eq projectId }
    }

    private fun getProjectFiles(projectId: Long): List<File> {
        val fileIds =
            ProjectFileExposedEntity
                .selectAll()
                .where { ProjectFileExposedEntity.projectId eq projectId }
                .map { it[ProjectFileExposedEntity.fileId] }

        if (fileIds.isEmpty()) return emptyList()

        return FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.id inList fileIds }
            .map { row ->
                File(
                    fileId = row[FileExposedEntity.id],
                    userId = row[FileExposedEntity.userId],
                    fileOriginalName = row[FileExposedEntity.originalName],
                    fileStoredName = row[FileExposedEntity.storedName],
                    fileUri = row[FileExposedEntity.uri],
                )
            }
    }

    private fun getProjectParticipants(projectId: Long): List<Member> {
        val memberIds =
            ProjectParticipantExposedEntity
                .selectAll()
                .where { ProjectParticipantExposedEntity.projectId eq projectId }
                .map { it[ProjectParticipantExposedEntity.memberId] }

        if (memberIds.isEmpty()) return emptyList()

        return MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.id inList memberIds }
            .map { row ->
                Member(
                    id = row[MemberExposedEntity.id],
                    name = row[MemberExposedEntity.name],
                    email = row[MemberExposedEntity.email],
                    grade = row[MemberExposedEntity.grade],
                    classNumber = row[MemberExposedEntity.classNumber],
                    number = row[MemberExposedEntity.number],
                    role = row[MemberExposedEntity.role],
                )
            }
    }
}
