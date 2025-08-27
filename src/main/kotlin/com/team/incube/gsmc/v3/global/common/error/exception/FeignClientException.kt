package com.team.incube.gsmc.v3.global.common.error.exception

import org.springframework.http.HttpStatus

class FeignClientException(
    message: String,
    val status: HttpStatus,
) : RuntimeException(message)
