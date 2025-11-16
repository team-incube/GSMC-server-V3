package com.team.incube.gsmc.v3.domain.project.service

import com.team.incube.gsmc.v3.domain.project.dto.Project
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SearchProjectService {
    fun execute(
        title: String?,
        pageable: Pageable,
    ): Page<Project>
}
