package com.team.incube.gsmc.v3.domain.evidence.entity.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("evidenceDraft")
data class EvidenceDraftRedisEntity(
    @Id
    val memberId: Long,
    val title: String = "",
    val content: String = "",
    val fileIds: List<Long> = emptyList(),
)
