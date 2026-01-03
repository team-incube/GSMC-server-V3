package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetStudentPercentResponse

interface FindMyPercentInGradeService {
    fun execute(includeApprovedOnly: Boolean = true): GetStudentPercentResponse
}
