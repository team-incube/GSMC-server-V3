package com.team.incube.gsmc.v3.domain.project.repository.impl

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.file.dto.File
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.project.dto.Project
import com.team.incube.gsmc.v3.domain.project.entity.ProjectExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectFileExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectParticipantExposedEntity
import com.team.incube.gsmc.v3.domain.project.repository.ProjectExposedRepository
import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
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
            ownerId = projectRow[ProjectExposedEntity.owner],
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
                .where { ProjectExposedEntity.owner eq ownerId }
                .toList()

        if (projectRows.isEmpty()) return emptyList()

        val projectIds = projectRows.map { it[ProjectExposedEntity.id] }
        val filesMap = getFilesByProjectIds(projectIds)
        val participantsMap = getParticipantsByProjectIds(projectIds)

        return projectRows.map { projectRow ->
            val projectId = projectRow[ProjectExposedEntity.id]

            Project(
                id = projectId,
                ownerId = projectRow[ProjectExposedEntity.owner],
                title = projectRow[ProjectExposedEntity.title],
                description = projectRow[ProjectExposedEntity.description],
                files = filesMap[projectId] ?: emptyList(),
                participants = participantsMap[projectId] ?: emptyList(),
            )
        }
    }

    override fun findProjectsByParticipantId(participantId: Long): List<Project> {
        val projectIds =
            ProjectParticipantExposedEntity
                .selectAll()
                .where { ProjectParticipantExposedEntity.member eq participantId }
                .map { it[ProjectParticipantExposedEntity.project] }

        if (projectIds.isEmpty()) return emptyList()

        val projectRows =
            ProjectExposedEntity
                .selectAll()
                .where { ProjectExposedEntity.id inList projectIds }
                .toList()

        val filesMap = getFilesByProjectIds(projectIds)
        val participantsMap = getParticipantsByProjectIds(projectIds)

        return projectRows.map { projectRow ->
            val projectId = projectRow[ProjectExposedEntity.id]

            Project(
                id = projectId,
                ownerId = projectRow[ProjectExposedEntity.owner],
                title = projectRow[ProjectExposedEntity.title],
                description = projectRow[ProjectExposedEntity.description],
                files = filesMap[projectId] ?: emptyList(),
                participants = participantsMap[projectId] ?: emptyList(),
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
                .toList()

        if (projectRows.isEmpty()) return PageImpl(emptyList(), pageable, total)

        val projectIds = projectRows.map { it[ProjectExposedEntity.id] }
        val filesMap = getFilesByProjectIds(projectIds)
        val participantsMap = getParticipantsByProjectIds(projectIds)

        val projects =
            projectRows.map { projectRow ->
                val projectId = projectRow[ProjectExposedEntity.id]

                Project(
                    id = projectId,
                    ownerId = projectRow[ProjectExposedEntity.owner],
                    title = projectRow[ProjectExposedEntity.title],
                    description = projectRow[ProjectExposedEntity.description],
                    files = filesMap[projectId] ?: emptyList(),
                    participants = participantsMap[projectId] ?: emptyList(),
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
                it[this.owner] = ownerId
                it[this.title] = title
                it[this.description] = description
            } get ProjectExposedEntity.id

        if (fileIds.isNotEmpty()) {
            ProjectFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[ProjectFileExposedEntity.project] = projectId
                this[ProjectFileExposedEntity.file] = fileId
            }
        }

        // 대표자를 참여자에 자동으로 포함
        val allParticipantIds = (participantIds + ownerId).distinct()

        if (allParticipantIds.isNotEmpty()) {
            ProjectParticipantExposedEntity.batchInsert(allParticipantIds) { memberId ->
                this[ProjectParticipantExposedEntity.project] = projectId
                this[ProjectParticipantExposedEntity.member] = memberId
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
        ownerId: Long,
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project {
        ProjectExposedEntity.update({ ProjectExposedEntity.id eq id }) {
            it[this.title] = title
            it[this.description] = description
        }

        ProjectFileExposedEntity.deleteWhere { project eq id }
        ProjectParticipantExposedEntity.deleteWhere { project eq id }

        if (fileIds.isNotEmpty()) {
            ProjectFileExposedEntity.batchInsert(fileIds) { fileId ->
                this[ProjectFileExposedEntity.project] = id
                this[ProjectFileExposedEntity.file] = fileId
            }
        }

        val allParticipantIds = (participantIds + ownerId).distinct()

        if (allParticipantIds.isNotEmpty()) {
            ProjectParticipantExposedEntity.batchInsert(allParticipantIds) { memberId ->
                this[ProjectParticipantExposedEntity.project] = id
                this[ProjectParticipantExposedEntity.member] = memberId
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

    override fun existsProjectParticipantByProjectIdAndMemberId(
        projectId: Long,
        memberId: Long,
    ): Boolean =
        !ProjectParticipantExposedEntity
            .select(ProjectParticipantExposedEntity.project)
            .where {
                (ProjectParticipantExposedEntity.project eq projectId) and
                    (ProjectParticipantExposedEntity.member eq memberId)
            }.limit(1)
            .empty()

    override fun findProjectTitleById(projectId: Long): String? =
        ProjectExposedEntity
            .select(ProjectExposedEntity.title)
            .where { ProjectExposedEntity.id eq projectId }
            .singleOrNull()
            ?.get(ProjectExposedEntity.title)

    override fun findProjectTitleAndValidateParticipant(
        projectId: Long,
        memberId: Long,
    ): String? =
        ProjectExposedEntity
            .innerJoin(ProjectParticipantExposedEntity)
            .select(ProjectExposedEntity.title)
            .where {
                (ProjectExposedEntity.id eq projectId) and
                    (ProjectParticipantExposedEntity.member eq memberId)
            }.singleOrNull()
            ?.get(ProjectExposedEntity.title)

    override fun deleteProjectById(projectId: Long) {
        ProjectFileExposedEntity.deleteWhere { ProjectFileExposedEntity.project eq projectId }
        ProjectParticipantExposedEntity.deleteWhere { ProjectParticipantExposedEntity.project eq projectId }
        ProjectExposedEntity.deleteWhere { id eq projectId }
    }

    private fun getProjectFiles(projectId: Long): List<File> {
        val fileIds =
            ProjectFileExposedEntity
                .selectAll()
                .where { ProjectFileExposedEntity.project eq projectId }
                .map { it[ProjectFileExposedEntity.file] }

        if (fileIds.isEmpty()) return emptyList()

        return FileExposedEntity
            .selectAll()
            .where { FileExposedEntity.id inList fileIds }
            .map { it.toFile() }
    }

    private fun getProjectParticipants(projectId: Long): List<Member> {
        val memberIds =
            ProjectParticipantExposedEntity
                .selectAll()
                .where { ProjectParticipantExposedEntity.project eq projectId }
                .map { it[ProjectParticipantExposedEntity.member] }

        if (memberIds.isEmpty()) return emptyList()

        return MemberExposedEntity
            .selectAll()
            .where { MemberExposedEntity.id inList memberIds }
            .map { it.toMember() }
    }

    override fun findScoreIdsByProjectTitle(projectTitle: String): List<Long> =
        ScoreExposedEntity
            .select(ScoreExposedEntity.id)
            .where {
                (ScoreExposedEntity.categoryEnglishName eq CategoryType.PROJECT_PARTICIPATION.englishName) and
                    (ScoreExposedEntity.activityName eq projectTitle)
            }.map { it[ScoreExposedEntity.id] }

    private fun getFilesByProjectIds(projectIds: List<Long>): Map<Long, List<File>> {
        val fileRelations =
            ProjectFileExposedEntity
                .selectAll()
                .where { ProjectFileExposedEntity.project inList projectIds }
                .map { it[ProjectFileExposedEntity.project] to it[ProjectFileExposedEntity.file] }

        if (fileRelations.isEmpty()) return emptyMap()

        val fileIds = fileRelations.map { it.second }.distinct()
        val filesById =
            FileExposedEntity
                .selectAll()
                .where { FileExposedEntity.id inList fileIds }
                .associate { row ->
                    row[FileExposedEntity.id] to row.toFile()
                }

        return fileRelations
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, fileIdList) ->
                fileIdList.mapNotNull { filesById[it] }
            }
    }

    private fun getParticipantsByProjectIds(projectIds: List<Long>): Map<Long, List<Member>> {
        if (projectIds.isEmpty()) return emptyMap()

        val participantData =
            MemberExposedEntity
                .innerJoin(ProjectParticipantExposedEntity)
                .selectAll()
                .where { ProjectParticipantExposedEntity.project inList projectIds }
                .map { row ->
                    row[ProjectParticipantExposedEntity.project] to row.toMember()
                }

        return participantData
            .groupBy({ it.first }, { it.second })
    }

    private fun ResultRow.toMember(): Member =
        Member(
            id = this[MemberExposedEntity.id],
            name = this[MemberExposedEntity.name],
            email = this[MemberExposedEntity.email],
            grade = this[MemberExposedEntity.grade],
            classNumber = this[MemberExposedEntity.classNumber],
            number = this[MemberExposedEntity.number],
            role = this[MemberExposedEntity.role],
        )

    private fun ResultRow.toFile(): File =
        File(
            id = this[FileExposedEntity.id],
            member = this[FileExposedEntity.member],
            originalName = this[FileExposedEntity.originalName],
            storeName = this[FileExposedEntity.storeName],
            uri = this[FileExposedEntity.uri],
        )
}
