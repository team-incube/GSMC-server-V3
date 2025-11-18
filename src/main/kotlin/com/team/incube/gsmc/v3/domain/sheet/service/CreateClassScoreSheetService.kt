package com.team.incube.gsmc.v3.domain.sheet.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity

interface CreateClassScoreSheetService {
    fun execute(
        grade: Int,
        classNumber: Int,
    ): ResponseEntity<ByteArrayResource>
}
