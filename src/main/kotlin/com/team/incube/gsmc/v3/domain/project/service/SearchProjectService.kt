package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.presentation.data.response.SearchProjectResponse
import org.springframework.data.domain.Pageable

interface SearchProjectService {
    fun execute(
        title: String?,
        pageable: Pageable,
    ): SearchProjectResponse
}
