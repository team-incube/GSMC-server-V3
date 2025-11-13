package com.team.incube.gsmc.v3.domain.auth.repository

import com.team.incube.gsmc.v3.domain.auth.entity.RefreshTokenRedisEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRedisRepository : CrudRepository<RefreshTokenRedisEntity, String> {
}
