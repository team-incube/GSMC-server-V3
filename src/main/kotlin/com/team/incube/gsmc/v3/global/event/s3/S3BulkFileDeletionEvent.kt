package com.team.incube.gsmc.v3.global.event.s3

data class S3BulkFileDeletionEvent(
    val fileUris: List<String>,
)
