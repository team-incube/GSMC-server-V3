package com.team.incube.gsmc.v3.global.common.error.handler

import com.team.incube.gsmc.v3.global.common.error.discord.DiscordErrorNotificationService
import com.team.incube.gsmc.v3.global.common.error.exception.FeignClientException
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import com.team.incube.gsmc.v3.global.config.logger
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@EnableWebMvc
@RestControllerAdvice
class GlobalExceptionHandler(
    private val discordErrorNotificationService: DiscordErrorNotificationService? = null,
) {
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
            message = "요청 본문을 읽을 수 없습니다.",
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): CommonApiResponse<Nothing> {
        warnTrace("HTTP Method Not Supported", ex)
        return CommonApiResponse.error(
            message = "지원하지 않는 HTTP 메서드입니다: ${ex.method}",
            status = HttpStatus.METHOD_NOT_ALLOWED,
        )
    }

    @ExceptionHandler(FeignClientException::class)
    fun handleFeignClientException(ex: FeignClientException): CommonApiResponse<Nothing> {
        warnTrace("FeignClientException", ex)
        return CommonApiResponse.error(ex.message ?: "Feign 클라이언트 오류", ex.status)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(ex: MethodArgumentTypeMismatchException): CommonApiResponse<Nothing> {
        warnTrace("Method Argument Type Mismatch", ex)
        val paramName = ex.name
        val requiredType = ex.requiredType?.simpleName ?: "Unknown"
        val providedValue = ex.value
        return CommonApiResponse.error(
            message = "잘못된 요청 파라미터입니다. '$paramName'는 $requiredType 타입이어야 하지만 '$providedValue'가 제공되었습니다.",
            status = HttpStatus.BAD_REQUEST,
        )
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
                    "HTTP Method" to getCurrentHttpMethod(),
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

        val globalError = bindingResult.globalErrors.firstOrNull()
        if (globalError != null) {
            return globalError.defaultMessage ?: "유효성 검사에 실패했습니다"
        }

        val fieldError = bindingResult.fieldErrors.firstOrNull()
        if (fieldError != null) {
            return fieldError.defaultMessage ?: "유효성 검사에 실패했습니다"
        }

        return "유효성 검사에 실패했습니다"
    }

    private fun <T> getRequestInfo(extractor: (jakarta.servlet.http.HttpServletRequest) -> T?, errorString: String): String {
        return try {
            val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
            if (request != null) {
                extractor(request)?.toString() ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            errorString
        }
    }
    private fun getCurrentHttpMethod(): String =
        getRequestInfo({ it.method }, "Unable to get HTTP method")

    private fun getCurrentRequestUri(): String =
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            requestAttributes?.request?.requestURI ?: "Unknown"
        } catch (e: Exception) {
            "Unable to get request URI"
        }
}
