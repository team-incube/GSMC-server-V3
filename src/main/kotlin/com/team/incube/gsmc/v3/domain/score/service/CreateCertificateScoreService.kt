package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse

interface CreateCertificateScoreService {
    fun execute(
        certificateName: String,
        fileId: Long,
    ): CreateScoreResponse
}
