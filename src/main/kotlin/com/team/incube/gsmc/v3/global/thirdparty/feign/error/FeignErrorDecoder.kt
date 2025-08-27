package com.team.incube.gsmc.v3.global.thirdparty.feign.error

import com.team.incube.gsmc.v3.global.common.error.exception.FeignClientException
import com.team.incube.gsmc.v3.global.config.logger
import feign.FeignException
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.http.HttpStatus
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

class FeignErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String?,
        response: Response,
    ): Exception {
        val status = response.status()
        if (status < 400) return FeignException.errorStatus(methodKey, response)

        val req = response.request()
        val (userMessage, httpStatus) = status.toUserMessageAndStatus()
        val errorBody = response.safeBodyAsString()

        logger().error(
            "Feign 클라이언트 오류 - methodKey={}, {} {} -> status={}, reason={}",
            methodKey,
            req.httpMethod().name,
            req.url(),
            status,
            response.reason(),
        )
        logger().error("응답 헤더: {}", response.headers())
        logger().error("응답 본문: {}", errorBody)
        logRequestDetails(response, methodKey)

        return FeignClientException(userMessage, httpStatus)
    }

    private fun Int.toUserMessageAndStatus(): Pair<String, HttpStatus> =
        when (this) {
            400 -> "잘못된 요청입니다." to HttpStatus.BAD_REQUEST
            401 -> "인증이 필요합니다." to HttpStatus.UNAUTHORIZED
            403 -> "접근이 거부되었습니다." to HttpStatus.FORBIDDEN
            404 -> "요청하신 리소스를 찾을 수 없습니다." to HttpStatus.NOT_FOUND
            429 -> "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요." to HttpStatus.TOO_MANY_REQUESTS
            500 -> "외부 서비스 내부 오류가 발생했습니다." to HttpStatus.INTERNAL_SERVER_ERROR
            502 -> "게이트웨이 오류가 발생했습니다." to HttpStatus.BAD_GATEWAY
            503 -> "서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해 주세요." to HttpStatus.SERVICE_UNAVAILABLE
            else -> "외부 요청 처리 중 오류가 발생했습니다." to HttpStatus.INTERNAL_SERVER_ERROR
        }

    private fun Response.safeBodyAsString(): String =
        runCatching {
            body()?.asInputStream()?.use { StreamUtils.copyToString(it, StandardCharsets.UTF_8) }
        }.getOrElse {
            logger().warn("오류 응답 본문을 읽는 데 실패했습니다", it)
            null
        } ?: "응답 본문을 읽을 수 없습니다"

    private fun logRequestDetails(
        response: Response,
        methodKey: String?,
    ) {
        runCatching {
            val req = response.request()
            val bodyStr = req.body()?.let { String(it, StandardCharsets.UTF_8) }
            logger().error("요청 정보 - methodKey={}, {} {}", methodKey, req.httpMethod().name, req.url())
            logger().error("요청 헤더: {}", req.headers())
            bodyStr?.let { logger().error("요청 본문: {}", it) }
        }.onFailure { logger().warn("요청 상세 로깅에 실패했습니다", it) }
    }
}
