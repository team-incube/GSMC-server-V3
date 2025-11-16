package com.team.incube.gsmc.v3.domain.project.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.team.incube.gsmc.v3.domain.project.dto.Project

interface SearchProjectService {
    fun execute(
        title: String?,
        pageable: Pageable,
    ): Page<Project>
}