package com.team.incube.gsmc.v3.global.common.error.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.incube.gsmc.v3.global.common.error.discord.DiscordErrorNotificationService
import com.team.incube.gsmc.v3.global.common.error.exception.FeignClientException
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import com.team.incube.gsmc.v3.global.config.logger
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@EnableWebMvc
@RestControllerAdvice
class GlobalExceptionHandler(
    private val discordErrorNotificationService: DiscordErrorNotificationService? = null,
) {
    companion object {
        private val objectMapper = ObjectMapper()
    }

    private fun warnTrace(
        prefix: String,
        ex: Throwable,
    ) {
        logger().warn("$prefix : {}", ex.message)
        logger().trace("$prefix Details : ", ex)
    }

    @ExceptionHandler(GsmcException::class)
    fun handleGsmcException(ex: GsmcException): CommonApiResponse<Nothing> {
        warnTrace("GsmcException", ex)
        return CommonApiResponse.error(ex.message ?: "알 수 없는 오류", ex.statusCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): CommonApiResponse<Nothing> {
        warnTrace("Validation Failed", ex)
        return CommonApiResponse.error(
            message = createValidationErrorMessage(ex),
            status = HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): CommonApiResponse<Nothing> {
        warnTrace("HttpMessageNotReadable", ex)
        return CommonApiResponse.error(
            message = "요청 본문을 읽을 수 없습니다: ${ex.mostSpecificCause.message}",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): CommonApiResponse<Nothing> {
        warnTrace("Field validation failed", ex)
        return CommonApiResponse.error(
            message = "필드 유효성 검사 실패 : ${ex.message}",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(FeignClientException::class)
    fun handleFeignClientException(ex: FeignClientException): CommonApiResponse<Nothing> {
        warnTrace("FeignClientException", ex)
        return CommonApiResponse.error(ex.message ?: "Feign 클라이언트 오류", ex.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception): CommonApiResponse<Nothing> {
        logger().error("Unhandled Exception Occur : ", ex)

        discordErrorNotificationService?.notifyError(
            exception = ex,
            context = "핸들링되지 않은 예외 발생",
            additionalInfo =
                mapOf(
                    "Exception Type" to (ex::class.simpleName ?: "Unknown"),
                    "Thread" to Thread.currentThread().name,
                    "Request URI" to getCurrentRequestUri(),
                ),
        )
        return CommonApiResponse.error(
            message = "서버 내부 오류가 발생했습니다.",
            status = HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): CommonApiResponse<Nothing> {
        warnTrace("Not Found Endpoint", ex)
        return CommonApiResponse.error(ex.message ?: "올바르지 않은 엔드포인트입니다", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(ex: MaxUploadSizeExceededException): CommonApiResponse<Nothing> {
        warnTrace("The file is too big", ex)
        return CommonApiResponse.error(
            message = "파일이 너무 큽니다, 최대 파일 용량 : ${ex.maxUploadSize}",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    private fun createValidationErrorMessage(ex: MethodArgumentNotValidException): String {
        val bindingResult = ex.bindingResult
        val objectName = bindingResult.objectName

        val errorMap =
            buildMap<String, Any> {
                bindingResult.globalErrors.firstOrNull()?.let { put(objectName, it.defaultMessage ?: "유효성 검사 오류") }
                val fieldErrors =
                    bindingResult.fieldErrors
                        .associate { it.field to (it.defaultMessage ?: "유효성 검사 오류") }
                        .takeIf { it.isNotEmpty() }
                fieldErrors?.let { put(objectName, it) }
            }

        return runCatching { objectMapper.writeValueAsString(errorMap).replace('"', '\'') }
            .getOrElse {
                logger().error("Error serializing validation errors", it)
                "Validation failed"
            }
    }

    private fun getCurrentRequestUri(): String =
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            requestAttributes?.request?.requestURI ?: "Unknown"
        } catch (e: Exception) {
            "Unable to get request URI"
        }
}
