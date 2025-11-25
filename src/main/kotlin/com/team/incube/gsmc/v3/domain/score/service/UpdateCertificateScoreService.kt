package com.team.incube.gsmc.v3.domain.score.service

interface UpdateCertificateScoreService {
    fun execute(
        scoreId: Long,
        value: String,
        fileId: Long,
    )
}
