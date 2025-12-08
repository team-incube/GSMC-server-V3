package com.team.incube.gsmc.v3.domain.sheet.service

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity

interface CreateGradeScoreSheetService {
    fun execute(grade: Int): ResponseEntity<ByteArrayResource>
}