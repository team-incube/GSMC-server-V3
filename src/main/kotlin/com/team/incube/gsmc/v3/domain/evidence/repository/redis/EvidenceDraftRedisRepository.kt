package com.team.incube.gsmc.v3.domain.evidence.repository.redis

import com.team.incube.gsmc.v3.domain.evidence.entity.redis.EvidenceDraftRedisEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EvidenceDraftRedisRepository : CrudRepository<EvidenceDraftRedisEntity, Long>
