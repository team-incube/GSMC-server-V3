package com.team.incube.gsmc.v3.domain.project.repository

import com.team.incube.gsmc.v3.domain.project.entity.ProjectDraftRedisEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectDraftRedisRepository : CrudRepository<ProjectDraftRedisEntity, Long>
