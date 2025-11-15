package com.team.incube.gsmc.v3.domain.auth.service

interface SignUpService {
    fun execute(
        name: String,
        studentNumber: Int,
    )
}
