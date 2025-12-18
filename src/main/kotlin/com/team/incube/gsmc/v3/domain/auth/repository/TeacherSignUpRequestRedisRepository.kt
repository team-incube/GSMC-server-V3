package com.team.incube.gsmc.v3.domain.auth.repository

import com.team.incube.gsmc.v3.domain.auth.entity.TeacherSignUpRequestRedisEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TeacherSignUpRequestRedisRepository : CrudRepository<TeacherSignUpRequestRedisEntity, Long>
