package com.team.incube.gsmc.v3.global.common.error.exception

import org.springframework.http.HttpStatus

class FeignClientException(
    errorMessage: String,
    val status: HttpStatus,
) : RuntimeException(errorMessage) {
    val statusCode: HttpStatus
        get() = status
}
