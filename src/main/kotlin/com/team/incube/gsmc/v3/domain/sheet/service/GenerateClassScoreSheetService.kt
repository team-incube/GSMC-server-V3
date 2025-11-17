package com.team.incube.gsmc.v3.domain.sheet.service

import org.springframework.core.io.ByteArrayResource

interface GenerateClassScoreSheetService {
    fun execute(
        grade: Int,
        classNumber: Int,
    ): ByteArrayResource
}
