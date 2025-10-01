package com.team.incube.gsmc.v3.domain.evidence.presentation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v3/evidences")
class EvidenceController()
{
    @GetMapping("/{evidenceId}")
    fun getEvidence(@PathVariable evidenceId: String): Nothing = TODO()
}
