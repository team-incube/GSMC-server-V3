package com.team.incube.gsmc.v3.domain.project.repository.redis

import com.team.incube.gsmc.v3.domain.project.entity.redis.ProjectDraftRedisEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectDraftRedisRepository : CrudRepository<ProjectDraftRedisEntity, Long>
