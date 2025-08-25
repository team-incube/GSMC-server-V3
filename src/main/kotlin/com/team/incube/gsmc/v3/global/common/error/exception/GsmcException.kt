package com.team.incube.gsmc.v3.global.common.error.exception

import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import org.springframework.http.HttpStatus

open class GsmcException(val errorCode: ErrorCode) : RuntimeException(errorCode.message) {
    val statusCode: HttpStatus
        get() = HttpStatus.valueOf(errorCode.status)
}