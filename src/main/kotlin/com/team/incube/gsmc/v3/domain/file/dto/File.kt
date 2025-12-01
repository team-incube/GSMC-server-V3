package com.team.incube.gsmc.v3.domain.file.dto

data class File(
    val id: Long,
    val member: Long,
    val originalName: String,
    val storeName: String,
    val uri: String,
)
