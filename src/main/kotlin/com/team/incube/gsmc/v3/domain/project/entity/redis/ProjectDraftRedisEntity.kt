package com.team.incube.gsmc.v3.domain.project.entity.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("projectDraft")
data class ProjectDraftRedisEntity(
    @Id
    val memberId: Long,
    val title: String,
    val description: String,
    val fileIds: List<Long>,
    val participantIds: List<Long>,
)
