package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse

interface CreateVolunteerScoreService {
    fun execute(value: String, memberId: Long): CreateScoreResponse
}
